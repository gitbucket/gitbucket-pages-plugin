package gitbucket.plugin.service

import gitbucket.plugin.model.{ Page, PageSourceType }
import gitbucket.plugin.model.Profile._
import gitbucket.plugin.model.Profile.profile.blockingApi._

trait PagesService {

  def getPageSource(userName: String, repositoryName: String)(implicit s: Session): PageSourceType =
    getPageOptions(userName, repositoryName).map(_.source).getOrElse(PageSourceType.GH_PAGES)

  def getPageOptions(userName: String, repositoryName: String)(implicit s: Session): Option[Page] =
    Pages.filter(t => (t.userName === userName.bind) && (t.repositoryName === repositoryName.bind)).firstOption

  def registerPageOptions(userName: String, repositoryName: String, source: PageSourceType)(implicit s: Session): Unit =
    Pages.insert(Page(userName, repositoryName, source))

  def updatePageOptions(userName: String, repositoryName: String, source: PageSourceType)(implicit s: Session): Unit =
    Pages
      .filter(t => (t.userName === userName.bind) && (t.repositoryName === repositoryName.bind))
      .map(t => t.source)
      .update(source)

  def renameRepository(userName: String, oldRepositoryName: String, newRepositoryName: String)(implicit s: Session): Unit =
    Pages
      .filter(t => (t.userName === userName.bind) && (t.repositoryName === oldRepositoryName.bind))
      .map(t => t.repositoryName)
      .update(newRepositoryName)

  def renameUserName(oldUserName: String, newUserName: String, repositoryName: String)(implicit s: Session): Unit =
    Pages
      .filter(t => (t.userName === oldUserName.bind) && (t.repositoryName === repositoryName.bind))
      .map(t => t.userName)
      .update(newUserName)

  def deletePageOptions(userName: String, repositoryName: String)(implicit s: Session): Unit = {
    Pages.filter(t => (t.userName === userName.bind) && (t.repositoryName === repositoryName.bind)).delete
  }
}
