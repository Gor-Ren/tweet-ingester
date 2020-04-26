import sbt._

object Dependencies {

  object Version {
    val Scala = "2.13.2"
    val Cats = "2.1.1"
    val Kittens = "2.1.0"
    val Fs2 = "2.2.2"
    val Http4s = "0.21.1"
    val Circe = "0.13.0"
    val CirceFs2 = "0.13.0"
    val Fs2Kafka = "1.0.0"
    val Refined = "0.9.13"
    val Newtype = "0.4.3"
    val PureConfig = "0.12.3"
    val Log4Cats = "1.0.1"
    val Logback = "1.2.3"
    val ScalaTest = "3.0.8"
    val ScalaCheck = "1.14.0"
    val ScalaMock = "4.4.0"
  }

  val dependencies = List(
    "org.typelevel" %% "cats-core" % Version.Cats,
    "org.typelevel" %% "cats-effect" % Version.Cats,
    "org.typelevel" %% "kittens" % Version.Kittens,
    "co.fs2" %% "fs2-core" % Version.Fs2,
    "org.http4s" %% "http4s-circe" % Version.Http4s,
    "org.http4s" %% "http4s-dsl" % Version.Http4s,
    "org.http4s" %% "http4s-blaze-server" % Version.Http4s,
    "org.http4s" %% "http4s-blaze-client" % Version.Http4s,
    "io.circe" %% "circe-generic" % Version.Circe,
    "io.circe" %% "circe-fs2" % Version.CirceFs2,
    "io.circe" %% "circe-refined" % Version.CirceFs2,
    "com.github.fd4s" %% "fs2-kafka" % Version.Fs2Kafka,
    "eu.timepit" %% "refined" % Version.Refined,
    "eu.timepit" %% "refined-scalacheck" % Version.Refined,
    "eu.timepit" %% "refined-pureconfig" % Version.Refined,
    "eu.timepit" %% "refined-cats" % Version.Refined,
    "io.estatico" %% "newtype" % Version.Newtype,
    "com.github.pureconfig" %% "pureconfig" % Version.PureConfig,
    "com.github.pureconfig" %% "pureconfig-cats" % Version.PureConfig,
    "io.chrisdavenport" %% "log4cats-slf4j" % Version.Log4Cats,
    "io.chrisdavenport" %% "log4cats-testing" % Version.Log4Cats,
    "ch.qos.logback" % "logback-classic" % Version.Logback
  )

  val testDependencies = List(
    "org.scalatest" %% "scalatest" % Version.ScalaTest,
    "org.scalacheck" %% "scalacheck" % Version.ScalaCheck,
    "org.scalamock" %% "scalamock" % Version.ScalaMock
  ).map(_ % "it,test")
}
