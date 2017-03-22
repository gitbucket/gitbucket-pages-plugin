package gitbucket.plugin.pages

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{ AccountService, RepositoryService }
import gitbucket.core.util.Implicits._
import gitbucket.core.util.SyntaxSugars._
import gitbucket.core.util.{ Directory, JGitUtil, OwnerAuthenticator, ReferrerAuthenticator }
import gitbucket.pages.html
import gitbucket.plugin.model.PageSourceType
import gitbucket.plugin.service.PagesService
import io.github.gitbucket.scalatra.forms._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.scalatra.i18n.Messages

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

  case class OptionsForm(source: PageSourceType)

  val optionsForm = mapping(
    "source" -> trim(label("Pages Source", text(required, pagesOption)))
  )(
    (source) => OptionsForm(PageSourceType.valueOf(source))
  )

  val PAGES_BRANCHES = List("gb-pages", "gh-pages")

  get("/:owner/:repository/pages/*")(referrersOnly { repository =>
    val path = params("splat")
    using(Git.open(Directory.getRepositoryDir(repository.owner, repository.name))) { git =>

      def resolvePath(objectId: Option[(String, ObjectId)], path: String) = {
        objectId match {
          case Some((path0, objId)) =>
            // redirect [owner/repo/pages/path] -> [owner/repo/pages/path/]
            if (shouldRedirect(path, path0)) {
              redirect(s"/${repository.owner}/${repository.name}/pages/$path/")
            } else {
              JGitUtil.getObjectLoaderFromId(git, objId) { loader =>
                contentType = Option(servletContext.getMimeType(path0)).getOrElse("application/octet-stream")
                response.setContentLength(loader.getSize.toInt)
                loader.copyTo(response.getOutputStream)
              }
              ()
            }
          case None =>
            NotFound()
        }
      }

      val source = getPageOptions(repository.owner, repository.name) match {
        case Some(p) => p.source
        case None => PageSourceType.GH_PAGES
      }
      source match {
        case PageSourceType.GH_PAGES =>
          val objectId = PAGES_BRANCHES.collectFirst(resolveBranch(git, _))
            .map(JGitUtil.getRevCommitFromId(git, _))
            .flatMap { revCommit =>
              getPathIndexObjectId(git, path, revCommit)
            }
          resolvePath(objectId, path)
        case PageSourceType.MASTER =>
          val objectId = Option(git.getRepository.resolve("master"))
            .map(JGitUtil.getRevCommitFromId(git, _))
            .flatMap { revCommit =>
              getPathIndexObjectId(git, path, revCommit)
            }
          resolvePath(objectId, path)
        case PageSourceType.MASTER_DOCS =>
          val objectId = Option(git.getRepository.resolve("master"))
            .map(JGitUtil.getRevCommitFromId(git, _))
            .flatMap { revCommit =>
              getPathIndexObjectId(git, s"docs/$path", revCommit)
            }
          resolvePath(objectId, path)
        case PageSourceType.NONE =>
          NotFound()
      }
    }
  })

  get("/:owner/:repository/pages")(referrersOnly { repository =>
    redirect(s"/${repository.owner}/${repository.name}/pages/")
  })

  get("/:owner/:repository/settings/pages")(ownerOnly { repository =>
    val source = getPageOptions(repository.owner, repository.name) match {
      case Some(p) => p.source
      case None => PageSourceType.GH_PAGES
    }
    html.options(repository, source, flash.get("info"))
  })

  post("/:owner/:repository/settings/pages", optionsForm)(ownerOnly { (form, repository) =>
    updatePageOptions(repository.owner, repository.name, form.source)
    flash += "info" -> "Pages source saved"
    redirect(s"/${repository.owner}/${repository.name}/settings/pages")
  })

  def resolveBranch(git: Git, name: String) = Option(git.getRepository.resolve(name))

  def shouldRedirect(path: String, path0: String) =
    !isRoot(path) && path0 != path && path0.startsWith(path) && !path.endsWith("/")

  def getPathIndexObjectId(git: Git, path: String, revCommit: RevCommit) = {
    getPathObjectId0(git, path, revCommit)
      .orElse(getPathObjectId0(git, appendPath(path, "index.html"), revCommit))
      .orElse(getPathObjectId0(git, appendPath(path, "index.htm"), revCommit))
  }

  def getPathObjectId0(git: Git, path: String, revCommit: RevCommit) = {
    getPathObjectId(git, path, revCommit).map(path -> _)
  }

  def appendPath(base: String, suffix: String): String = {
    if (isRoot(base)) suffix
    else if (base.endsWith("/")) base + suffix
    else base + "/" + suffix
  }

  def isRoot(path: String) = path == ""

  private def pagesOption: Constraint = new Constraint() {
    override def validate(name: String, value: String, messages: Messages): Option[String] =
      PageSourceType.valueOpt(value) match {
        case Some(_) => None
        case None => Some("Pages source is invalid.")
      }
  }
}


object PagesControllerBase {
  implicit class listCollectFirst[A](private val lst: List[A]) extends AnyVal {
    @tailrec
    final def collectFirst[B](f: A => Option[B]): Option[B] = {
      lst match {
        case head :: tail =>
          f(head) match {
            case Some(x) => Some(x)
            case None => tail.collectFirst(f)
          }
        case Nil => None
      }
    }
  }
}
