package gitbucket.plugin.pages

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.util.Implicits._
import gitbucket.core.util.{Directory, JGitUtil, OwnerAuthenticator, ReferrerAuthenticator}
import gitbucket.pages.html
import gitbucket.plugin.model.PageSourceType
import gitbucket.plugin.service.PagesService
import org.scalatra.forms._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.scalatra.i18n.Messages
import scala.util.Using

import scala.annotation.tailrec
import scala.language.implicitConversions

class PagesController
    extends PagesControllerBase
    with AccountService
    with OwnerAuthenticator
    with PagesService
    with RepositoryService
    with ReferrerAuthenticator

trait PagesControllerBase extends ControllerBase {
  self: AccountService with RepositoryService with PagesService with ReferrerAuthenticator with OwnerAuthenticator =>
  import PagesControllerBase._

  val optionsForm = mapping("source" -> trim(label("Pages Source", text(required, pagesOption))))((source) =>
    OptionsForm(PageSourceType.valueOf(source))
  )

  val PAGES_BRANCHES = List("gb-pages", "gh-pages")

  get("/:owner/:repository/pages/*")(referrersOnly { repository =>
    renderPage(repository, params("splat"))
  })

  get("/:owner/:repository/pages")(referrersOnly { repository =>
    renderPage(repository, "")
  })

  private def renderPage(repository: RepositoryInfo, path: String) = {
    val defaultBranch = repository.repository.defaultBranch
    Using.resource(Git.open(Directory.getRepositoryDir(repository.owner, repository.name))) { git =>
      getPageSource(repository.owner, repository.name) match {
        case PageSourceType.GH_PAGES =>
          renderFromBranch(repository, git, path, PAGES_BRANCHES.collectFirstOpt(resolveBranch(git, _)))
        case PageSourceType.MASTER =>
          renderFromBranch(repository, git, path, resolveBranch(git, defaultBranch))
        case PageSourceType.MASTER_DOCS =>
          renderFromBranch(repository, git, joinPath("docs", path), resolveBranch(git, defaultBranch))
        case PageSourceType.NONE =>
          NotFound()
      }
    }
  }

  get("/:owner/:repository/settings/pages")(ownerOnly { repository =>
    val source = getPageSource(repository.owner, repository.name)
    val defaultBranch = repository.repository.defaultBranch
    html.options(repository, source, defaultBranch, flash.get("info"))
  })

  post("/:owner/:repository/settings/pages", optionsForm)(ownerOnly { (form, repository) =>
    updatePageOptions(repository.owner, repository.name, form.source)
    flash.update("info", "Pages source saved")
    redirect(s"/${repository.owner}/${repository.name}/settings/pages")
  })

  def renderPageObject(git: Git, path: String, obj: ObjectId): Unit = {
    JGitUtil.getObjectLoaderFromId(git, obj) { loader =>
      contentType = guessContentType(path)
      response.setContentLength(loader.getSize.toInt)
      loader.copyTo(response.getOutputStream)
    }
  }

  def renderFromBranch(
    repository: RepositoryService.RepositoryInfo,
    git: Git,
    path: String,
    branchObject: Option[ObjectId]
  ): Any = {
    val pagePair = branchObject
      .map(JGitUtil.getRevCommitFromId(git, _))
      .flatMap(getPageObjectId(git, path, _))

    pagePair match {
      case Some((realPath, _)) if shouldRedirect(path, realPath) =>
        redirect(s"/${repository.owner}/${repository.name}/pages/$path/")
      case Some((realPath, pageObject)) =>
        renderPageObject(git, realPath, pageObject)
      case None =>
        NotFound()
    }
  }

  def resolveBranch(git: Git, name: String) = Option(git.getRepository.resolve(name))

  // redirect [owner/repo/pages/path] -> [owner/repo/pages/path/]
  def shouldRedirect(path: String, path0: String): Boolean =
    !isRoot(path) && path0 != path && path0.startsWith(path) && !path.endsWith("/")

  def getPageObjectId(git: Git, path: String, revCommit: RevCommit): Option[(String, ObjectId)] = {
    listProbablePages(path).collectFirstOpt(getPathObjectIdPair(git, _, revCommit))
  }

  def listProbablePages(path: String): List[String] = {
    path :: List("index.html", "index.htm").map(joinPath(path, _))
  }

  def getPathObjectIdPair(git: Git, path: String, revCommit: RevCommit): Option[(String, ObjectId)] = {
    getPathObjectId(git, path, revCommit).map(path -> _)
  }

  def joinPath(base: String, suffix: String): String = {
    val sfx = suffix.stripPrefix("/")
    if (isRoot(base)) sfx
    else base.stripSuffix("/") + "/" + sfx
  }

  def isRoot(path: String): Boolean = path == ""

  def guessContentType(path: String): String = {
    Option(servletContext.getMimeType(path)).getOrElse("application/octet-stream")
  }

}

object PagesControllerBase {
  case class OptionsForm(source: PageSourceType)

  implicit class listCollectFirst[A](private val lst: List[A]) extends AnyVal {
    @tailrec
    final def collectFirstOpt[B](f: A => Option[B]): Option[B] = {
      lst match {
        case head :: tail =>
          f(head) match {
            case Some(x) => Some(x)
            case None    => tail.collectFirstOpt(f)
          }
        case Nil => None
      }
    }
  }

  def pagesOption: Constraint = new Constraint() {
    override def validate(name: String, value: String, messages: Messages): Option[String] =
      PageSourceType.valueOpt(value) match {
        case Some(_) => None
        case None    => Some("Pages source is invalid.")
      }
  }
}
