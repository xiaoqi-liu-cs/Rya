# Experiment Datasets

This directory contains the scripts and instructions for acquiring and preprocessing the datasets required to reproduce the execution time overhead experiments across systems like Weave or Rya. See our paper for more details.

## 1. Required Datasets

To fully replicate the benchmark experiments, the following original datasets are needed:
* **NYC TLC Yellow Taxi Trip Records (2020 - 2025):** Used for large-scale sequential scanning and aggregation benchmarks.
* **Enron Spam Email Dataset:** Used for text processing and distributed join performance testing.
* **Pokec Social Network Dataset:** Used for graph traversal and random memory access benchmarking.

## 2. Environment Setup

To avoid `externally-managed-environment` errors on modern Linux distributions, data processing libraries must be installed in a Python virtual environment. 

You can either set this up manually using the commands below, or simply run the automated processing script in **Step B** (which handles virtual environment creation and dependency installation for you).

```bash
# Manually create and activate the virtual environment
python3 -m venv venv
source venv/bin/activate

# Install required packages
pip install pandas pyarrow fastparquet
```

## 3. Step-by-Step Data Preparation
### Step A: Download the Raw Data
Use the provided bash script `download_datasets_NY_Taxi.sh` to download the Yellow Taxi Parquet files.
**Important**: Make sure you have enough disk space (~46GB) before downloading the full 2020-2025 dataset.

To choose what to download, open `download_datasets_NY_Taxi.sh` in a text editor and modify the MODE variable at the top of the file:
* MODE="month": Downloads a single month (edit YEAR and MONTH variables).
* MODE="single_year": Downloads a whole year (edit SINGLE_YEAR variable).
* MODE="multi_year": Downloads multiple years (edit START_YEAR and END_YEAR variables).

After configuring the script, execute it:
```bash
chmod +x download_datasets_NY_Taxi.sh
./download_datasets_NY_Taxi.sh
```
*(Place raw datasets in the raw_data/ directory manually if not using an automated script for them)*.

### Step B: Merge Parquet Files and Convert to CSV
We provide two ways to process the downloaded Taxi datasets:

#### Option 1: Automated Pipeline (Recommended)
You can run the all-in-one bash script. It will automatically set up the virtual environment, merge the 2020 data, merge the 2021-2025 data into an aggregated file, and convert all merged Parquet files into standard .csv files.

```bash
chmod +x process_datasets_NY_Taxi.sh
./process_datasets_NY_Taxi.sh
```
Example:
python3 process_datasets_NY_Taxi.py convert_to_csv raw_data/yellow_tripdata_2020-01.parquet processed_data/yellow_tripdata_2020-01.csv

#### Option 2: Manual Step-by-Step Execution
If you prefer to process specific files manually, make sure your **virtual environment** is activated, then use the Python CLI commands:
* 1. Merge a Single Year (e.g., 2020):
```bash
python3 process_datasets_NY_Taxi.py merge_single_year 2020
```
* 2. Merge Multiple Years (e.g., 2021-2025):
```bash
python3 process_datasets_NY_Taxi.py merge_multi_year 2021 2025
```
* 3. Convert any Parquet file to CSV:
```bash
python3 process_datasets_NY_Taxi.py convert_to_csv raw_data/yellow_tripdata_2020-01.parquet processed_data/yellow_tripdata_2020-01.csv
```

### Step C: Rya Protocol Preprocessing (Important)
The generated `.csv` files contain raw plaintext strings. Since the Rya protocol relies on oblivious algorithmic methods like OBIN, it operates on integers data!
* Before feeding the CSVs into Rya:
 - Navigate to the **Step0_Client_Data_Process** module.
 - Execute the parsing logic to map and convert all relevant string columns from the .csv files into integers.
 - The resulting integer-based dataset is what should be used for the **Rya** execution pipeline.
