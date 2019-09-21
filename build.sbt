import com.typesafe.sbt.packager.docker.DockerChmodType

val appName = "tweet-ingester"

val Http4sVersion = "0.21.0-M5"
val CirceVersion = "0.12.1"
val CirceFs2Version = "0.12.0"
val ScalaTestVersion = "3.0.8"
val ScalaCheckVersion = "1.14.0"
val ScalaMockVersion = "4.4.0"
val PureConfigVersion = "0.12.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := appName,
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.13.0",
    mainClass in Compile := Some("dev.rennie.tweetingester.TweetIngester"),
    resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-fs2" % CirceFs2Version,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion
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
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings( // Docker settings
    packageName := appName,
    dockerBaseImage := "openjdk:11",
    daemonUserUid := None,
    daemonUser := "daemon",
    dockerChmodType := DockerChmodType.UserGroupWriteExecute // give write permissions
  )

unmanagedResourceDirectories in Test += baseDirectory.value / "test" / "resources"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)

scalacOptions in (Compile, doc) += "-no-link-warnings"
