organization  := "gitbucket"
name          := "pages-plugin"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

resolvers ++= Seq(
  Resolver.jcenterRepo,
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "io.github.gitbucket"   %%  "gitbucket"           % "4.6.0",
  "javax.servlet"         %   "javax.servlet-api"   % "3.1.0"
)

// bintrayReleaseOnPublish in ThisBuild := false
bintrayOmitLicense := true
bintrayRepository := "gitbucket-pages-plugin"
