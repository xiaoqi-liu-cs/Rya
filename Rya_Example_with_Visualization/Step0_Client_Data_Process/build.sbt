name := "ClientProcessData"
version := "0.1.0"
scalaVersion := "2.12.17"

Compile / mainClass := Some("ClientDataProcess.Main")

fork := true
outputStrategy := Some(StdoutOutput)

// --- Configuration ---
// MODE can be "PreProcess" or "AfterProcess"
// INPUT_FILE applies to PreProcess
// OUTPUT_INT_FILE applies to AfterProcess

javaOptions ++= Seq(
  "-DMODE=PreProcess",
  
  "-DBASE_NAME=Example", // baseName is only used for naming the processedData
  "-DINPUT_FILE=Example.txt",
  
  "-DOUTPUT_INT_FILE=Example_output_int.txt"
  // "-DMAPPING_FILE=Example_mapping.csv" // Optional, automatically is {baseName}_mapping.csv
)
