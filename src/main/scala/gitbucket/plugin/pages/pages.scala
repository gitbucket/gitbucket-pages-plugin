package gitbucket.plugin.pages

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{ AccountService, RepositoryService }
import gitbucket.core.util.Implicits._
import gitbucket.core.util.SyntaxSugars._
import gitbucket.core.util.{ Directory, JGitUtil, ReferrerAuthenticator }
import gitbucket.plugin.model.PageSourceType
import gitbucket.plugin.service.PagesService
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit

import scala.annotation.tailrec
import scala.language.implicitConversions

class PagesController
  extends PagesControllerBase
  with AccountService
  with PagesService
  with RepositoryService
  with ReferrerAuthenticator

trait PagesControllerBase extends ControllerBase {
  self: AccountService with RepositoryService with PagesService with ReferrerAuthenticator =>

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
          val objectId = resolveBranch(git, PAGES_BRANCHES)
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

  @tailrec
  final def resolveBranch(git: Git, names: List[String]): Option[ObjectId] = {
    names match {
      case name :: rest =>
        val objectId = Option(git.getRepository.resolve(name))
        if (objectId.isEmpty) resolveBranch(git, rest)
        else objectId
      case Nil => None
    }
  }

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

}

