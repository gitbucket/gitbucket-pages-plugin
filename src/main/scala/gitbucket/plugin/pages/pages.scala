package gitbucket.plugin.pages

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{ AccountService, RepositoryService }
import gitbucket.core.util.ControlUtil._
import gitbucket.core.util.{ Directory, JGitUtil, ReferrerAuthenticator }
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk

import scala.language.implicitConversions

class PagesController
  extends ControllerBase
  with AccountService
  with RepositoryService
  with ReferrerAuthenticator

trait PagesControllerBase {
  self: ControllerBase with AccountService with RepositoryService with ReferrerAuthenticator =>

  val PAGES_BRANCH = "gh-pages"

  get("/:owner/:repository/pages/*")(referrersOnly { repository =>
    val path = params("splat")
    using(Git.open(Directory.getRepositoryDir(repository.owner, repository.name))) { git =>
      val objectId = Option(git.getRepository.resolve(PAGES_BRANCH))
        .map(JGitUtil.getRevCommitFromId(git, _))
        .flatMap { revCommit =>
          getPathIndexObjectId(git, path, revCommit)
        }

      objectId match {
        case Some((path0, objId)) =>
          // redirect [owner/repo/pages/path] -> [owner/repo/pages/path/]
          if (path0 != path && path0.startsWith(path) && !path.endsWith("/")) {
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
  })

  get("/:owner/:repository/pages")(referrersOnly { repository =>
    redirect(s"/${repository.owner}/${repository.name}/pages/")
  })

  def getPathIndexObjectId(git: Git, path: String, revCommit: RevCommit) = {
    getPathObjectId0(git, path, revCommit)
      .orElse(getPathObjectId0(git, appendPath(path, "index.html"), revCommit))
      .orElse(getPathObjectId0(git, appendPath(path, "index.htm"), revCommit))
  }

  def getPathObjectId0(git: Git, path: String, revCommit: RevCommit) = {
    getPathObjectId(git, path, revCommit).map(path -> _)
  }

  def appendPath(base: String, suffix: String): String = {
    if (base == "") suffix
    else if (base.endsWith("/")) base + suffix
    else base + "/" + suffix
  }

  // copy&paste from RepositoryViewerControllerBase
  private def getPathObjectId(git: Git, path: String, revCommit: RevCommit): Option[ObjectId] = {
    @scala.annotation.tailrec
    def _getPathObjectId(path: String, walk: TreeWalk): Option[ObjectId] = walk.next match {
      case true if walk.getPathString == path => Some(walk.getObjectId(0))
      case true => _getPathObjectId(path, walk)
      case false => None
    }

    using(new TreeWalk(git.getRepository)) { treeWalk =>
      treeWalk.addTree(revCommit.getTree)
      treeWalk.setRecursive(true)
      _getPathObjectId(path, treeWalk)
    }
  }
}

