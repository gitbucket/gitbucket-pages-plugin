organization  := "gitbucket"

name          := "pages-plugin"

version       := "0.4"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

resolvers += Resolver.jcenterRepo

resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

libraryDependencies ++= Seq(
  "gitbucket"          % "gitbucket-assembly" % "3.13.0"  % "provided",
  "javax.servlet"      % "javax.servlet-api"  % "3.1.0"   % "provided"
)
