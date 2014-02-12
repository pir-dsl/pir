import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object PirBuild extends Build {
  
  // Target JVM version
  val SCALAC_JVM_VERSION = "jvm-1.6"
  val JAVAC_JVM_VERSION = "1.6"
  
  lazy val root = Project("root", file("."), settings = rootSettings) aggregate(allProjects: _*)
  
  lazy val core = Project("core", file("core"), settings = coreSettings)
  
  lazy val assemblyProj = Project("assembly", file("assembly"), settings = assemblyProjSettings).dependsOn(core)
  
  lazy val allProjects = Seq[ProjectReference](core, assemblyProj)
	  
  def sharedSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.uwm.cs",
    version := "0.1.0",
    scalaVersion := "2.10.3",
    scalacOptions := Seq("-unchecked", "-optimize", "-deprecation", "-target:" + SCALAC_JVM_VERSION),
    javacOptions := Seq("-target", JAVAC_JVM_VERSION, "-source", JAVAC_JVM_VERSION),
    unmanagedBase <<= baseDirectory { base => base / "custom_lib" },
    unmanagedJars in Compile <<= baseDirectory map { base => (base ** "*.jar").classpath },
    retrieveManaged := true,
    retrievePattern := "[type]s/[artifact](-[revision])(-[classifier]).[ext]",
    transitiveClassifiers in Scope.GlobalScope := Seq("sources"),

    // Fork new JVMs for tests and set Java options for those
    //fork := true,
    //javaOptions += "-Xmx3g",

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.0" % "test",
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "com.novocode" % "junit-interface" % "0.9" % "test",
      "org.easymock" % "easymock" % "3.1" % "test"   
    ) 
  )
  
  def coreSettings = sharedSettings ++ Seq(
    name := "pir-core",
    
    libraryDependencies ++= Seq(
      //"colt" % "colt" % "1.2.0",
      "commons-io" % "commons-io" % "2.4",
      "org.apache.commons" % "commons-math3" % "3.0",
      "org.apache.lucene" % "lucene-core" % "3.6.1",
      "com.drewnoakes" % "metadata-extractor" % "2.6.2",
      "log4j" % "log4j" % "1.2.17",
	  "org.slf4j" % "slf4j-api" % "1.7.2",
	  "commons-logging" % "commons-logging" % "1.1.3",
	  "com.typesafe" % "config" % "1.0.2",
	  "com.amazonaws" % "aws-java-sdk" % "1.4.7",
	  "com.esotericsoftware.kryo" % "kryo" % "2.22"
    ) 
  )
  
  def rootSettings = sharedSettings ++ Seq(
    publish := {}
  )
  
  def assemblyProjSettings = sharedSettings ++ Seq(
    name := "pir-assembly",
    jarName in assembly <<= version map { v => "pir-assembly-" + v + ".jar" }
  ) ++ assemblySettings ++ extraAssemblySettings

  def extraAssemblySettings = Seq(
    test in assembly := {},
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  		{
    		case PathList("org", "apache", "spark", xs @ _*) => MergeStrategy.discard
    		case PathList("org", "scalatest", xs @ _*) => MergeStrategy.discard
    		case PathList("org", "apache", "hadoop", xs @ _*) => MergeStrategy.discard
    		case x => old(x)
  		}
	}
  )
  
}
