import gitbucket.core.controller.ControllerBase
import gitbucket.plugin.pages.PagesController
import gitbucket.plugin.pages.util.VersionUtil
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId = "pages"
  override val pluginName = "Pages Plugin"
  override val description = "Project pages for gitbucket"
  override val versions = List(new Version(VersionUtil.version.getOrElse("unknown")))

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController
  )
}

