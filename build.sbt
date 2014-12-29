import play.PlayScala
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

maintainer in Linux := "Ruben Barros <ruben.23@live.com.pt>"

packageSummary in Linux := "Backend-Fracker"

packageDescription := "API server for Fracker Mobile App"

name := """backend-fracker"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.reactivemongo"           %% "reactivemongo"            % "0.10.5.0.akka23",
  "io.argonaut"                 %% "argonaut"                 % "6.0.4",
  "com.typesafe.play"           %% "play-slick"               % "0.8.1",
  "org.slf4j"                    % "slf4j-nop"                % "1.6.4",
  "mysql"                        % "mysql-connector-java"     % "5.1.27"
)
