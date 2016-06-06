
import gitbucket.core.controller.ControllerBase
import gitbucket.plugin.pages.PagesController
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  import gitbucket.plugin.pages.util.VersionUtil
  override val pluginId = "pages"
  override val pluginName = "Pages Plugin"
  override val description = "Project pages for gitbucket"
  override val versions = List(new Version(VersionUtil.version.getOrElse("unknown")))

  override val controllers: Seq[(String, ControllerBase)] = Seq(
    "/*" -> new PagesController
  )
}

package gitbucket.plugin.pages.util {
  import scala.collection.JavaConverters._
  import scala.util.Try
  import java.util.jar.{ Manifest => JarManifest }

  object VersionUtil {
    val version = Try(getVersionFromManifests()).toOption.flatten
    def getVersionFromManifests() = {
      val resources = getClass.getClassLoader.getResources("META-INF/MANIFEST.MF").asScala.toList
      resources
        .flatMap(res => Try(new JarManifest(res.openStream())).toOption.toList)
        .find(manifest => Option(manifest.getMainAttributes.getValue("Implementation-Title")).contains("pages-plugin"))
        .flatMap(manifest => Option(manifest.getMainAttributes.getValue("Implementation-Version")))
    }
  }
}
