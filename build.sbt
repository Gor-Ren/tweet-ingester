val Http4sVersion = "0.20.8"
val ScalaTestVersion = "3.0.8"
val ScalaCheckVersion = "1.14.0"
val CirceVersion = "0.11.1"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := "tweet-ingester",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion,
      "org.scalacheck" %% "scalacheck" % ScalaCheckVersion
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
  "-Ypartial-unification",
  "-Xfatal-warnings"
)
