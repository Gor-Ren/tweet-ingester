import com.typesafe.sbt.packager.docker.DockerChmodType

val appName = "tweet-ingester"

val ScalaVersion = "2.13.0"
val CatsVersion = "2.1.0"
val Fs2Version = "2.2.2"
val Http4sVersion = "0.21.1"
val CirceVersion = "0.13.0"
val CirceFs2Version = "0.13.0"
val Fs2KafkaVersion = "1.0.0"
val RefinedVersion = "0.9.12"
val NewtypeVersion = "0.4.3"
val PureConfigVersion = "0.12.0"
val Log4CatsVersion = "1.0.1"
val LogbackVersion = "1.2.3"
val ScalaTestVersion = "3.0.8"
val ScalaCheckVersion = "1.14.0"
val ScalaMockVersion = "4.4.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := appName,
    version := "0.2-SNAPSHOT",
    scalaVersion := ScalaVersion,
    mainClass in Compile := Some("dev.rennie.tweetingester.TweetIngester"),
    resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0"),
      "org.typelevel" %% "cats-core" % CatsVersion,
      "org.typelevel" %% "cats-effect" % CatsVersion,
      "co.fs2" %% "fs2-core" % Fs2Version,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-fs2" % CirceFs2Version,
      "io.circe" %% "circe-refined" % CirceFs2Version,
      "com.github.fd4s" %% "fs2-kafka" % Fs2KafkaVersion,
      "eu.timepit" %% "refined" % RefinedVersion,
      "eu.timepit" %% "refined-scalacheck" % RefinedVersion,
      "io.estatico" %% "newtype" % NewtypeVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "io.chrisdavenport" %% "log4cats-slf4j" % Log4CatsVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion,
      "org.scalacheck" %% "scalacheck" % ScalaCheckVersion,
      "org.scalamock" %% "scalamock" % ScalaMockVersion
    ).map(_ % "it,test"),
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

scalacOptions += "-Ymacro-annotations"

scalacOptions in (Compile, doc) += "-no-link-warnings"
