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


# Align the arbitrary and messy data table formats to the same
# Used for merging datasets
def align_table_to_schema(table, target_schema):
    # 1. Change all column names to be lowercase
    new_names = [name.lower() for name in table.column_names]
    table = table.rename_columns(new_names)
    
    # 2. If any columns are missing for the current year-month data, fill with null values.
    for name in target_schema.names:
        if name not in table.schema.names:
            null_arr = pa.nulls(table.num_rows, type=target_schema.field(name).type)
            table = table.append_column(name, null_arr)
            
    # 3. Reorder the columns strictly according to the format.
    table = table.select(target_schema.names)
    
    # 4. Type casting (e.g., converting int32 to int64, or null to double)
    return table.cast(target_schema)


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
            table = pq.read_table(parquet_file)
            
            # Lowercase column names
            table = table.rename_columns([c.lower() for c in table.column_names])
            tables.append(table)
            print(f"  -> Loaded {parquet_file}")
        else:
            print(f"  -> Skipping missing file: {parquet_file}")
            
    if len(tables) > 0:
        # Extract the common compatible schema for all months of the current year
        schemas = [t.schema for t in tables]
        unified_schema = pa.unify_schemas(schemas)

        # Force all month data to be aligned to a compatible schema
        aligned_tables = [align_table_to_schema(t, unified_schema) for t in tables]
        
        # Merge tables with unified schema
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
        
        # Extract the common compatible schema for all years
        schemas = []
        for y_pq in yearly_parquets:
            schema = pq.read_schema(y_pq)
            schema = schema.with_names([c.lower() for c in schema.names])
            schemas.append(schema)
        
        unified_schema = pa.unify_schemas(schemas)
        
        # 2. Append the generated yearly parquets to the final multi-year parquet using ParquetWriter
        # Force alignment of data for each year to prevent cross-year schema conflicts.
        with pq.ParquetWriter(final_parquet, unified_schema) as writer:
            for y_pq in yearly_parquets:
                print(f"  -> Writing {os.path.basename(y_pq)} to aggregated file...")
                table = pq.read_table(y_pq)
                aligned_table = align_table_to_schema(table, unified_schema)
                writer.write_table(aligned_table)
                
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
