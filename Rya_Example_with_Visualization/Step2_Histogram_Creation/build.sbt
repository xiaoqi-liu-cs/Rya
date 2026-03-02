name := "Histogram_Creation"
version := "0.1.0"
scalaVersion := "2.12.17"

Compile / mainClass := Some("Rya.Main")

// --- Configuration ---

// Enable forking to pass Java options
fork := true

// Avoid the [info]'s
outputStrategy := Some(StdoutOutput)

// Configure Algorithm: "bucket" or "bitonic"
// Configure Batch Size (k): e.g., 4, 8, 16... (Should be the # of unique intermediate keys (defined in RYA paper by (k)))
// Default set to "numUniqueKeys", thus it will be calculated by the program automatically. You can also set it manually, but notice that:
// 1. Bin-and-Sum requires >= 2 items to form a bucket pair. (BATCH_SIZE must >= 2)
// 2. If BATCH_SIZE >= input size ($N). Only 1 partial batch will be processed, and the algorithm will be very slow.
// 3. If BATCH_SIZE < number of unique keys ($numUniqueKeys). Keys will be split across multiple batches (valid but less efficient).

javaOptions ++= Seq(
  "-DSORT_ALGO=bitonic",
  
  "-DBATCH_SIZE=numUniqueKeys",
    
  "-DBASE_NAME=Example",  
  "-DINPUT_FILE=Example_input_int.txt"
)
// Configure BASE_NAME is only used for naming the output.
// Configure INPUT_FILE: "Example_input_int.txt" must be input of Integer Array. 
// User can PreProcess String input by Step0: Client Side Data Process
