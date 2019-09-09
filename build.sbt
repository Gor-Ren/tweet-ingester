val Http4sVersion = "0.20.8"
val Specs2Version = "4.1.0"
val ScalaTestVersion = "3.0.8"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := "tweet-ingestor",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % ScalaTestVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "it,test"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0"),
    Defaults.itSettings
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)
