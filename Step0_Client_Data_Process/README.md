# ClientDataProcess Tool

## Purpose
In the RYA paper, the Pre-Process and After-Process steps are strictly performed on the **Client side** to guarantee complete obliviousness and performance on the untrusted Server.

This standalone Scala project is designed for processing data *before* running the protocol, completely separating the Client-Side cryptography/mapping logic from the Server-Side computation logic.

## How to Run
Dynamic naming is supported by `baseName=Example` in `build.sbt`

### Step 1: PreProcess (Run by Client)
Reads a plaintext file (e.g., `Example.txt`) and generates a integer array for the Server, plus a local mapping dictionary.
1. In `build.sbt`, set:
   * `-DMODE=PreProcess`
   * `-DINPUT_FILE=Example.txt`
2. Run the project by **sbt run**
3. Outputs generated:
   * `Example_Rya_Input.txt` (Upload this to the Server)
   * `Example_Rya_Mapping.csv` (Keep this secret on the Client!)

### Step 2: Server Computation (Run by Server)
Upload `Example_Rya_Input.txt` to the main RYA project, and run RYA protocol by **sbt run**
The Server calculates and returns `Example_Rya_Output.txt`.

### Step 3: AfterProcess (Run by Client)
Recovers the final plaintext string histogram from the Server's integer output.
1. In `build.sbt`, set:
   * `-DMODE=AfterProcess`
   * `-DOUTPUT_INT_FILE=Example_Rya_Output.txt`
2. Ensure `Example_mapping.csv` is in the directory.
3. Run the project by **sbt run**
4. Final Output:
   * Displays the top 10 results in the console.
   * Generates `Example_Rya_Output_Final.txt` containing the full, string histogram.
