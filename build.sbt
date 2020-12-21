import sbt.Keys.testFrameworks

val zioVersion    = "1.0.3"
val Http4sVersion = "0.21.8"
val CirceVersion  = "0.13.0"

inThisBuild(
  List(
    scalaVersion := "2.13.4",
    semanticdbEnabled := true,
    semanticdbVersion := "4.4.2"
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("fix", "all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll")
addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; compile:scalafix --check; test:scalafix --check")

lazy val `word-counter` = project
  .in(file("."))
  .settings(
    name := "word-counter",
    version := "0.1.0",
    scalaVersion := "2.13.4",
    libraryDependencies ++=
      Seq(
        "dev.zio"      % "zio_2.13"             % zioVersion,
        "dev.zio"      % "zio-streams_2.13"     % zioVersion,
        "dev.zio"      % "zio-test_2.13"        % zioVersion % "test",
        "dev.zio"      % "zio-test-sbt_2.13"    % zioVersion % "test",
        "dev.zio"     %% "zio-process"          % "0.3.0",
        "dev.zio"     %% "zio-interop-cats"     % "2.2.0.1",
        "org.http4s"  %% "http4s-blaze-server"  % Http4sVersion,
        "org.http4s"  %% "http4s-blaze-client"  % Http4sVersion,
        "org.http4s"  %% "http4s-circe"         % Http4sVersion,
        "org.http4s"  %% "http4s-dsl"           % Http4sVersion,
        "io.circe"    %% "circe-generic"        % CirceVersion,
        "io.circe"    %% "circe-parser"         % CirceVersion,
        "io.circe"    %% "circe-generic-extras" % CirceVersion,
        "com.novocode" % "junit-interface"      % "0.11"     % "test"
      ),
    scalacOptions += "-Ywarn-unused",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
