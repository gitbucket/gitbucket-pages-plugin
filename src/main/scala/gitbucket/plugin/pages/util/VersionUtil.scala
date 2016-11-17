package gitbucket.plugin.pages.util

import java.util.jar.{ Manifest => JarManifest }

import scala.util.Try
import scala.collection.JavaConverters._

object VersionUtil {
  val version = Try(parseManifest()).toOption.flatten

  def parseManifest() = {
    val resources = getClass.getClassLoader.getResources("META-INF/MANIFEST.MF").asScala.toList
    resources
      .flatMap(res => Try(new JarManifest(res.openStream())).toOption.toList)
      .find(manifest => Option(manifest.getMainAttributes.getValue("Implementation-Title")).contains("pages-plugin"))
      .flatMap(manifest => Option(manifest.getMainAttributes.getValue("Implementation-Version")))
  }
}
