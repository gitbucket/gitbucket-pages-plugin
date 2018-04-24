package gitbucket.plugin.pages

import gitbucket.core.plugin.RepositoryHook
import gitbucket.plugin.model.PageSourceType
import gitbucket.plugin.model.Profile.profile.blockingApi._
import gitbucket.plugin.service.PagesService

class PagesHook extends PagesHookBase with PagesService

trait PagesHookBase extends RepositoryHook {
  self: PagesService =>

  override def created(owner: String, repository: String)(implicit session: Session): Unit =
    registerPageOptions(owner, repository, PageSourceType.GH_PAGES)

  override def deleted(owner: String, repository: String)(implicit session: Session): Unit =
    deletePageOptions(owner, repository)

  override def renamed(owner: String, oldRepository: String, newRepository: String)(implicit session: Session): Unit =
    renameRepository(owner, oldRepository, newRepository)

  override def transferred(oldOwner: String, newOwner: String, repository: String)(implicit session: Session): Unit =
    renameUserName(oldOwner, newOwner, repository)

  override def forked(owner: String, newOwner: String, repository: String)(implicit session: Session): Unit = {
    val source = getPageSource(owner, repository)
    registerPageOptions(newOwner, repository, source)
  }
}
