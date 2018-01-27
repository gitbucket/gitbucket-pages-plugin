package gitbucket.plugin.model

trait PagesComponent { self: gitbucket.core.model.Profile =>
  import profile.api._

  implicit val psColumnType = MappedColumnType.base[PageSourceType, String](ps => ps.code, code => PageSourceType.valueOf(code))

  lazy val Pages = TableQuery[Pages]

  class Pages(tag: Tag) extends Table[Page](tag, "PAGES") {
    val userName = column[String]("USER_NAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val source = column[PageSourceType]("SOURCE")
    def * = (userName, repositoryName, source) <> ((Page.apply _).tupled, Page.unapply)
  }
}

abstract sealed case class PageSourceType(code: String)

object PageSourceType {
  object NONE extends PageSourceType("none")
  object MASTER_DOCS extends PageSourceType("master /docs")
  object MASTER extends PageSourceType("master")
  object GH_PAGES extends PageSourceType("gh-pages")

  val values: Vector[PageSourceType] = Vector(NONE, MASTER_DOCS, MASTER, GH_PAGES)

  private val map: Map[String, PageSourceType] = values.map(enum => enum.code -> enum).toMap

  def apply(code: String): PageSourceType = map(code)

  def valueOf(code: String): PageSourceType = map(code)
  def valueOpt(code: String): Option[PageSourceType] = map.get(code)
}

case class Page(
  userName: String,
  repositoryName: String,
  source: PageSourceType)
