
import gitbucket.core.controller.ControllerBase
import gitbucket.plugin.pages.PagesController
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId = "pages"
  override val pluginName = "Pages Plugin"
  override val description = "Provides Pages feature on GitBucket"
  override val versions = List(new Version("0.0.0"))

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController
  )
}