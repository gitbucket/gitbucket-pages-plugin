
import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.Version
import gitbucket.plugin.pages.PagesController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "pages"
  override val pluginName: String = "Pages Plugin"
  override val versions: Seq[Version] = Seq(Version(3, 9))
  override val description: String = "Provides Pages feature on GitBucket"

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController
  )
}