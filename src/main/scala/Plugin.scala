import gitbucket.core.controller.ControllerBase
import gitbucket.plugin.pages.{ PagesController, PagesHook }
import io.github.gitbucket.solidbase.migration.{ SqlMigration, LiquibaseMigration }
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId = "pages"
  override val pluginName = "Pages Plugin"
  override val description = "Project pages for gitbucket"
  override val versions = List(
    new Version("0.1"),
    new Version("0.2"),
    new Version("0.3"),
    new Version("0.4"),
    new Version("0.5"),
    new Version("0.6"),
    new Version("0.7"),
    new Version("0.8"),
    new Version("0.9"),
    new Version("1.0"),
    new Version(
      "1.1",
      new LiquibaseMigration("update/gitbucket-page_1.1.xml"),
      new SqlMigration("update/gitbucket-page_1.1.sql")
    )
  )

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController
  )

  override val repositoryHooks = Seq(
    new PagesHook
  )
}

