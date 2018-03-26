import gitbucket.core.controller.Context
import gitbucket.core.controller.ControllerBase
import gitbucket.core.plugin.Link
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.plugin.pages.{ PagesController, PagesHook }
import io.github.gitbucket.solidbase.migration.{ SqlMigration, LiquibaseMigration }
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId = "pages"
  override val pluginName = "Pages Plugin"
  override val description = "Project pages for GitBucket"
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
      new SqlMigration("update/gitbucket-page_1.1.sql")),
    new Version("1.2"),
    new Version("1.3"),
    new Version("1.4.0"),
    new Version("1.5.0"),
    new Version("1.6.0"),
    new Version("1.7.0"))

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController)

  override val repositorySettingTabs = Seq(
    (repository: RepositoryInfo, context: Context) => Some(Link("pages", "Pages", s"settings/pages")))

  override val repositoryHooks = Seq(
    new PagesHook)
}

