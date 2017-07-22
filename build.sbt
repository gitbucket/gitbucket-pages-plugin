organization  := "gitbucket"
name          := "gitbucket-pages-plugin"
scalaVersion  := "2.12.2"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

enablePlugins(SbtTwirl)

resolvers ++= Seq(
  Resolver.jcenterRepo,
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "io.github.gitbucket"   %%  "gitbucket"           % "4.14.1",
  "javax.servlet"         %   "javax.servlet-api"   % "3.1.0"
)

