name := "RIS"

version := "1.0"

organization := "de.hci"

scalaVersion := "2.10.3"

autoScalaLibrary := true

autoCompilerPlugins := true

retrieveManaged := true

scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-unchecked"
)

scalaSource in Compile <<= baseDirectory(_ / "src")

javacOptions ++= Seq(
    "-encoding",
    "UTF-8"
)

libraryDependencies  <++= (scalaVersion){scalaV => Seq(
  "org.scala-lang"            % "scala-library"           % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "scala-compiler"          % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "scalap"                  % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "jline"                   % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "scala-swing"             % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "scala-reflect"           % scalaV withSources() withJavadoc(),
  "org.scala-lang"            % "scala-actors"            % scalaV withSources() withJavadoc(),
  "org.scala-lang.plugins"    % "continuations"           % scalaV withSources() withJavadoc(),
  "com.typesafe.akka"         %% "akka-actor"             % "latest.integration" withSources() withJavadoc(),
  "com.typesafe.akka"         %% "akka-agent"             % "latest.integration" withSources() withJavadoc(),
  //"com.ardor3d"               % "ardor3d-collada"        % "0.9" withSources() withJavadoc(),
  "org.lwjgl.lwjgl"           % "lwjgl"                   % "2.9.1" withSources() withJavadoc(),
  "org.lwjgl.lwjgl"           % "lwjgl_util"              % "2.9.1" withSources() withJavadoc()
)}

transitiveClassifiers := Seq(
	"sources",
	"javadoc"
)

unmanagedJars <<= baseDirectory map { base => ((base ** "lib") ** "*.jar").classpath }