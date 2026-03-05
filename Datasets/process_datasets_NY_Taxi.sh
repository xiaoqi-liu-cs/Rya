# Ensure virtual environment is activated if applicable
python3 -m venv venv
source venv/bin/activate

# Install pandas in the virtual environment
pip install pandas pyarrow

echo "========================================"
echo "Phase 1: Merging Parquet Datasets"
echo "========================================"

# Merge 2020 single year -> yellow_tripdata_2020_wy.parquet
python3 process_datasets.py merge_single_year 2020

# Merge 2021-2025 multi-year -> yellow_tripdata_20212025.parquet
python3 process_datasets.py merge_multi_year 2021 2025


echo ""
echo "========================================"
echo "Phase 2: Converting Parquet to CSV"
echo "========================================"

# Convert 2020 Parquet to CSV -> yellow_tripdata_2020_wy.csv
python3 process_datasets.py convert_to_csv processed_data/yellow_tripdata_2020_wy.parquet processed_data/yellow_tripdata_2020_wy.csv

# Convert 2021-2025 Parquet to CSV -> yellow_tripdata_20212025.csv
python3 process_datasets.py convert_to_csv processed_data/yellow_tripdata_20212025.parquet processed_data/yellow_tripdata_20212025.csv

# Quit virtual environment
deactivate

echo ""
echo "========================================"
echo "Reminder: The original data has been converted to plaintext CSV."
echo "Before inputting this into your Rya protocol, you MUST execute"
echo "Step0_Client_Data_Process to map and convert the String type fields"
echo "in the CSV into Integers format."
echo "========================================"
