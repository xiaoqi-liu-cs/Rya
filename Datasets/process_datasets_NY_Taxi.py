import pandas as pd
import os
import sys
import pyarrow.parquet as pq
import pyarrow as pa

RAW_DIR = "raw_data"
OUT_DIR = "processed_data"

# Convert NY_Taxi.parquet to NY_Taxi.csv
def convert_parquet_to_csv(parquet_path, csv_path):
    if not os.path.exists(parquet_path):
        print(f"  -> Error, file doesn't exist: {parquet_path}")
        return False
    
    try:
        print(f"  -> Converting {parquet_path} to CSV...")
        # Use pyarrow to read in batches to prevent Out-Of-Memory errors on massive files
        parquet_file = pq.ParquetFile(parquet_path)
        is_first_chunk = True
        
        for batch in parquet_file.iter_batches(batch_size=100000):
            df_chunk = batch.to_pandas()
            df_chunk.to_csv(
                csv_path, 
                mode='a' if not is_first_chunk else 'w', 
                header=is_first_chunk, 
                index=False
            )
            is_first_chunk = False
            
        print(f"  -> Success, save to: {csv_path}")
        return True
    except Exception as e:
        print(f"  -> Error, convert failed: {parquet_path}: {e}")
        return False


# Merge month 01 up to 12 together and output _wy.parquet
def merge_single_year(year, raw_dir, out_dir):
    print(f"\n=== Start merging full year data for {year} ===")
    out_parquet = os.path.join(out_dir, f"yellow_tripdata_{year}_wy.parquet")
    
    # If the file already exists, delete it first to avoid appending duplicate data
    if os.path.exists(out_parquet):
        os.remove(out_parquet)
    
    # Store data in memeory
    tables = []
    for month in range(1, 13):
        # Format month to two digits (e.g., 1 becomes 01)
        month_str = f"{month:02d}"
        parquet_file = os.path.join(raw_dir, f"yellow_tripdata_{year}-{month_str}.parquet")
        
        if os.path.exists(parquet_file):
            tables.append(pq.read_table(parquet_file))
            print(f"  -> Loaded {parquet_file}")
        else:
            print(f"  -> Skipping missing file: {parquet_file}")
            
    if len(tables) > 0:
        # Merge Parquet tables directly
        merged_table = pa.concat_tables(tables)
        pq.write_table(merged_table, out_parquet)
        print(f"==> {year} full year data merged, saved to: {out_parquet}")
        return out_parquet
    else:
        print(f"==> {year} no available data found.")
        return None

# Merge multiple years of data into a total file
def merge_multiple_years(start_year, end_year, raw_dir, out_dir):
    print(f"\n========================================")
    print(f"Start merging multiple years data {start_year}-{end_year}...")
    print(f"========================================")
    
    final_parquet = os.path.join(out_dir, f"yellow_tripdata_{start_year}{end_year}.parquet")
    
    # If the file already exists, delete it first to avoid appending duplicate data
    if os.path.exists(final_parquet):
        os.remove(final_parquet)
        
    yearly_parquets = []
    for year in range(start_year, end_year + 1):
        # 1. Call single year merge logic to generate yearly _wy.parquet
        yearly_pq = merge_single_year(year, raw_dir, out_dir)
        
        if yearly_pq and os.path.exists(yearly_pq):
            yearly_parquets.append(yearly_pq)
    
    # if len(yearly_parquets) > 0:
    if yearly_parquets:
        print(f"\n[*] Appending all yearly data to {start_year}-{end_year} aggregated set...")
        
        # Read the first table to get the schema for the ParquetWriter
        first_table = pq.read_table(yearly_parquets[0])
        
        # 2. Append the generated yearly parquets to the final multi-year parquet using ParquetWriter
        with pq.ParquetWriter(final_parquet, first_table.schema) as writer:
            for y_pq in yearly_parquets:
                print(f"  -> Writing {os.path.basename(y_pq)} to aggregated file...")
                table = pq.read_table(y_pq)
                writer.write_table(table)
                
        print(f"\n========================================")
        print(f"Multi-year data archiving completed: {final_parquet}")
        print("========================================")
    else:
        print("No valid yearly data found to merge.")


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    
    # Simple CLI routing
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "convert_to_csv" and len(sys.argv) == 4:
            input_file = sys.argv[2]
            output_file = sys.argv[3]
            convert_parquet_to_csv(input_file, output_file)
            
        elif command == "merge_single_year" and len(sys.argv) == 3:
            year = int(sys.argv[2])
            merge_single_year(year, RAW_DIR, OUT_DIR)
            
        elif command == "merge_multi_year" and len(sys.argv) == 4:
            start_yr = int(sys.argv[2])
            end_yr = int(sys.argv[3])
            merge_multiple_years(start_yr, end_yr, RAW_DIR, OUT_DIR)
            
        else:
            print("Invalid command or arguments. Usage:")
            print("  python process_datasets_NY_Taxi.py convert_to_csv <input.parquet> <output.csv>")
            print("  python process_datasets_NY_Taxi.py merge_single_year <year>")
            print("  python process_datasets_NY_Taxi.py merge_multi_year <start_year> <end_year>")
    else:
        print("Please provide a command. Usage:")
        print("  python process_datasets_NY_Taxi.py convert_to_csv <input.parquet> <output.csv>")
        print("  python process_datasets_NY_Taxi.py merge_single_year <year>")
        print("  python process_datasets_NY_Taxi.py merge_multi_year <start_year> <end_year>")
