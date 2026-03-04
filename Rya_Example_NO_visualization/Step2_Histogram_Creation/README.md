# Step 2: Oblivious Histogram Creation
*(Part of `Rya_Example_NO_Visualization`)*

## Purpose of this Project
This project is the **streamlined, performance-oriented implementation** of the Oblivious Histogram Creation phase for the RYA MapReduce protocol. 

To ensure strict compliance with Oblivious principles and to maximize execution speed, **all internal visualization, console logging, and debug prints have been stripped from this version.** It is designed to run efficiently on large datasets without the I/O bottleneck of console tracking.

## Looking for the Visualization Version?
> If you want to visually track data flows, step-by-step Butterfly Network routing, or debug how `OBIN` aligns distributed counts, please refer to the sister project located at `../Rya_Example_with_Visualization/Step2_Histogram_Creation`.

## ⚠️ Important: Input Data Format constraint
**This module operates STRICTLY on `Int` data (from 0 to numUniqueKeys!).** To guarantee security and eliminate $O(N)$ string comparison leakages on the Server side, this module does not process `String` or text data. 
If your raw dataset contains strings (e.g., CSV files) or doesn't fullfill the requirement of **(from 0 to numUniqueKeys)**, **you MUST first run `Step0_Client_Data_Process`** to map the strings to a dense integer array. Provide the resulting `_Rya_Input.txt` file as input to this step.

### Algorithms Inside
* **OHIST, OLABEL, OBINSUM, OCNT**: Implemented separately following the formal Rya paper specifications.
* **Oblivious Bin-Packing (OBIN)**: Safely aligns distributed, sparse counts into a unified global histogram.
* **OSORT**: Provides 2 options: **Bitonic Sort** (default) and **Oblivious Bucket Sort**.

## Output Files
If your `BASE_NAME=Example`, the system generates:
- `Example_Rya_Output.txt` (The finalized integer histogram)

*Note: Use `Step0_Client_Data_Process` in AfterProcess mode to translate this integer histogram back into human-readable string histogram.*

## Quick Start
Modify the parameters in `build.sbt`:
```scala
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
  "-DINPUT_FILE=Example_Rya_Output.txt"
)
// Configure BASE_NAME is only used for naming the output.
// Configure INPUT_FILE: "Example_Rya_Output.txt" must be input of Integer Array. 
// User can PreProcess String input by Step0: Client Side Data Process
```
Then run the program by **sbt run**
