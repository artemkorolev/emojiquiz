name := "emojiquiz"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeIvyRepo("releases")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV withSources() withJavadoc(),
    "io.spray"            %%  "spray-routing" % sprayV withSources() withJavadoc(),
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.slick" %% "slick" % "3.1.0",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "mysql"               % "mysql-connector-java" % "5.1.27",
    "info.mukel" %% "telegrambot4s" % "1.0.3-SNAPSHOT",
    "com.typesafe" %% "emoji" % "1.0.0"
  )
}

