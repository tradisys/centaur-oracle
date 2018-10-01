name := "CentaurOracle"

libraryDependencies ++= Seq(
  // Akka:
  "com.typesafe.akka" %% "akka-actor" % "2.5.13" % "provided",
  "com.typesafe.akka" %% "akka-http" % "10.1.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",

  // Other:
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scorexfoundation" %% "scrypto" % "2.0.0",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "com.wavesplatform" % "wavesj" % "0.9"
)

lazy val centaurOracle = (project in file("."))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "CentaurOracle",
    version := "1.0",
    scalaVersion := "2.12.4",
    mainClass in Compile := Some("com.tradisys.oracle.CentaurOracleApplication")
  )

val meta = """META.INF(.)*""".r

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) ⇒ MergeStrategy.discard
  case PathList("reference.conf") ⇒ MergeStrategy.concat
  case PathList("application.conf") ⇒ MergeStrategy.concat
  case x ⇒ val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run)).evaluated
