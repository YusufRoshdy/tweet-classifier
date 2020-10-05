name := "stream"

version := "1.0"

scalaVersion := "2.12.10"
val sparkVersion = "3.0.1"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion 
libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % sparkVersion % "provided"

