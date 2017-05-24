name := "emojiquiz"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeIvyRepo("releases")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "org.slf4j" % "slf4j-simple" % "1.6.4",
  "info.mukel" %% "telegrambot4s" % "2.9.0-SNAPSHOT",
  "com.lightbend" %% "emoji" % "1.1.1",
  "org.mongodb" %% "casbah" % "3.1.1",
  "com.vdurmont" % "emoji-java" % "3.1.0",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

