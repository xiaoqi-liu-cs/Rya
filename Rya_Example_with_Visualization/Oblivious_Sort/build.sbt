name := "ObliviousSort"
version := "0.1.0"
scalaVersion := "2.12.17"
organization := "org.apache.spark.shuffle.weave"

Compile / mainClass := Some("oblivious_sort.Main")

// Enable forking to pass Java options
fork := true

// Avoid the [info]'s
outputStrategy := Some(StdoutOutput)

// Configure Algorithm: "bucket" or "bitonic"
// Configure Input File: "input.txt" (or any other path)
javaOptions ++= Seq(
  "-DSORT_ALGO=bucket",
  "-DINPUT_FILE=input.txt"
)
