import com.typesafe.sbt.packager.docker.DockerChmodType

val appName = "tweet-ingester"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "dev.rennie",
    name := appName,
    version := "0.2-SNAPSHOT",
    scalaVersion := Dependencies.Version.Scala,
    mainClass in Compile := Some("dev.rennie.tweetingester.TweetIngester"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    libraryDependencies ++= Dependencies.dependencies,
    libraryDependencies ++= Dependencies.testDependencies,
    fork in run := true, // required for SBT to correctly allow IOApps to release resources on termination
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

unmanagedResourceDirectories in Test += baseDirectory.value / "test" / "resources" // use /test/resources/ files

scalacOptions += "-Ymacro-annotations"

scalacOptions in (Compile, doc) += "-no-link-warnings"
