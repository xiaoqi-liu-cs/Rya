# Step 2: Oblivious Histogram Creation
*(Part of `Rya_Example_with_Visualization`)*

## Purpose of this Project
This project serves as a **visualization prototype** for the Oblivious Histogram Creation phase of the RYA MapReduce protocol. 

Unlike the highly streamlined production version of Rya, **this project is intentionally packed with rich debug prints and state visualizations**. It is designed to help researchers visually track data flows, Butterfly Network routing, and how `OBIN` (Oblivious Bin Packing) aligns distributed counts without leaking memory access patterns.

## ⚠️ Important: Input Data Format constraint
**This module operates STRICTLY on `Int` data.** To guarantee security and eliminate $O(N)$ string comparison leakages on the Server side, this module does not process `String` or text data. 
If your raw dataset contains strings or text (e.g., CSV files), **you MUST first run `Step0_Client_Data_Process`** to map the strings to a dense integer array. Provide the resulting `_input_int.txt` file as input to this step.

### Algorithms Inside
* **OHIST, OLABEL, OBINSUM, OCNT**: Implemented separately following the formal Rya paper specifications.
* **Oblivious Bin-Packing (OBIN)**: Safely aligns distributed, sparse counts into a unified global histogram.
* **OSORT**: Provides 2 options: **Bitonic Sort** (default) and **Oblivious Bucket Sort**.

## Output Files
If your `BASE_NAME=Example`, the system generates:
- `Example_output_int.txt` (The finalized integer histogram)

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
  "-DINPUT_FILE=Example_input_int.txt"
)
// Configure BASE_NAME is only used for naming the output.
// Configure INPUT_FILE: "Example_input_int.txt" must be input of Integer Array. 
// User can PreProcess String input by Step0: Client Side Data Process
```
Then run the program by **sbt run**
