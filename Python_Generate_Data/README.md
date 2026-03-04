# Data Generation for Obliviousness Testing

## Purpose
This directory contains a suite of Python scripts designed to generate strictly sized input files with drastically different data distributions. These files are specifically engineered to test the **Obliviousness** of the Rya MapReduce protocol. 

In a perfectly oblivious system, processing datasets of the **exact same size**—regardless of whether the keys are highly skewed (all identical) or uniformly distributed (completely random)—should yield indistinguishable execution traces, memory access patterns, and network traffic.

## Generated Datasets
The generator creates files of exactly the same byte size (default is 50MB) but with different statistical distributions:
1. **`identical_int.txt`**: Highly skewed. Every item is exactly the same integer, except for one instance of every other integer to ensure the domain $[0, num_unique_keys-1]$ is covered.
2. **`identical+random_int.txt`**: Split distribution. The first half of the file consists of a single repeated integer, while the second half contains uniformly random integers.
3. **`random_int.txt`**: Uniform distribution. Completely random integers across the entire defined domain.
4. **`random_string.txt`** (Optional): Randomly generated alphabetical strings of lengths 1 to 5, could be use for the Step0_Client_Data_Process

## Usage
You can execute the entire generation pipeline with a single command:
```bash
./generate_data.sh
```

## Configuration
You can easily adjust the parameters at the top of the generate_data.sh file:

* SIZE_MB: Target file size in MB.
* NUM_UNIQUE_KEYS: The domain of integers to pick from (e.g., 100000 means keys range from 0 to 99999).
* FIXED_INT: The specific integer to repeat for the highly skewed test cases.

## Verification
* The bash script automatically verifies the byte-level size of all generated files (ls -l *.txt)
* The script uses countItems.py to output the exact total count and distinct count of items in each file, then automatically saves to countItems_log.txt.
