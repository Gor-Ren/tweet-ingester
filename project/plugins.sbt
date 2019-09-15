resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases"

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.8")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.4")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
