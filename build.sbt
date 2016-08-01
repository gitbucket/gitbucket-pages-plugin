organization  := "gitbucket"
name          := "pages-plugin"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

resolvers += Resolver.jcenterRepo
resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

libraryDependencies ++= Seq(
  "gitbucket"          % "gitbucket-assembly" % "4.3.0"   % "provided",
  "javax.servlet"      % "javax.servlet-api"  % "3.1.0"   % "provided"
)

// bintrayReleaseOnPublish in ThisBuild := false
bintrayOmitLicense := true
bintrayRepository := "gitbucket-pages-plugin"
