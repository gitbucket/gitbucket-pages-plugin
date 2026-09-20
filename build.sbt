organization := "gitbucket"
name := "gitbucket-pages-plugin"
scalaVersion := "2.13.18"
version := "1.11.0"
gitbucketVersion := "4.48.0"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

libraryDependencies += "org.scalatra" %% "scalatra-scalatest-javax" % "3.2.1" % Test

scalafmtOnCompile := true
