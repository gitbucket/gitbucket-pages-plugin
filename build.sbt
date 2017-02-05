organization  := "gitbucket"
name          := "pages-plugin"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

resolvers ++= Seq(
  Resolver.jcenterRepo,
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "io.github.gitbucket"   %%  "gitbucket"           % "4.9.0",
  "javax.servlet"         %   "javax.servlet-api"   % "3.1.0"
)

