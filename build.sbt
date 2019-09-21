val Http4sVersion = "0.21.0-M5"
val CirceVersion = "0.12.1"
val CirceFs2Version = "0.12.0"
val ScalaTestVersion = "3.0.8"
val ScalaCheckVersion = "1.14.0"
val ScalaMockVersion = "4.4.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := "tweet-ingester",
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.13.0",
    resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-fs2" % CirceFs2Version
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion,
      "org.scalacheck" %% "scalacheck" % ScalaCheckVersion,
      "org.scalamock" %% "scalamock" % ScalaMockVersion
    ).map(_ % "it,test"),
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
  "-Xfatal-warnings"
)
