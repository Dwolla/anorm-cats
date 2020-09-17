lazy val commonSettings = Seq(
  organization := "com.dwolla",
  homepage := Some(url("https://github.com/Dwolla/anorm-cats")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseCrossBuild := false,
  releaseProcess := {
    import sbtrelease.ReleaseStateTransformations._
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  },
  startYear := Option(2020),
  libraryDependencies ++= {
    Seq(
      "org.playframework.anorm" %% "anorm" % "2.6.5" exclude("joda-time", "joda-time") exclude("org.joda", "joda-convert"),
    )
  },
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  Compile / scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
      case _ => Nil
    }
  },

  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => Nil
      case _ => compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) :: Nil
    }
  },
)

lazy val bintraySettings = Seq(
  bintrayVcsUrl := homepage.value.map(_.toString),
  bintrayRepository := "maven",
  bintrayOrganization := Option("dwolla"),
  pomIncludeRepository := { _ => false }
)

lazy val `anorm-cats` = (project in file("core"))
  .settings(Seq(
    description := "Cats typeclass instances for Anorm types",
    libraryDependencies ++= {
      val catsV = "2.2.0"

      Seq(
        "org.typelevel"           %% "cats-core"                  % catsV,
        "org.typelevel"           %% "cats-laws"                  % catsV % Test,
        "org.typelevel"           %% "cats-kernel-laws"           % catsV % Test,
        "org.typelevel"           %% "discipline-specs2"          % "1.1.0" % Test,
      )
    },
  ) ++ commonSettings ++ bintraySettings: _*)
  .dependsOn(`anorm-cats-testkit` % "test -> test")

lazy val `anorm-cats-testkit` = (project in file("testkit"))
  .settings(Seq(
    description := "Testing helpers",
  ) ++ commonSettings ++ bintraySettings: _*)

lazy val `anorm-cats-root` = (project in file("."))
  .settings(Seq(
    crossScalaVersions := Seq.empty,
  ) ++ commonSettings ++ noPublishSettings: _*)
  .aggregate(`anorm-cats`, `anorm-cats-testkit`)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  Keys.`package` := file(""),
)
