ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.example"

lazy val tlspark = (project in file("."))
  .settings(
    name := "TudLabSpark",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.1",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.1",
    libraryDependencies += "com.google.cloud.spark" %% "spark-bigquery-with-dependencies" % "0.19.1",
    assembly / assemblyJarName := "tlspark.jar",
    //assembly / logLevel := Level.Debug,
    assembly / assemblyMergeStrategy := {
        case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*)  => MergeStrategy.first
        case PathList("META-INF", "native", xs @ _*)  => MergeStrategy.deduplicate
        case PathList("META-INF", xs @ _*) => MergeStrategy.discard
        case PathList("reference.conf") => MergeStrategy.concat
        case PathList("codegen", xs @ _*)    => MergeStrategy.discard
        case PathList("javax", xs @ _*)    => MergeStrategy.last
        case PathList("org", "aopalliance", xs @ _*)    => MergeStrategy.discard
        case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
        case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.discard
        case "jetty-dir.css" => MergeStrategy.discard
        case "module-info.class" => MergeStrategy.discard
        case "git.properties"    => MergeStrategy.discard
        case x => MergeStrategy.deduplicate
    }
  )