import random
import os
import time
import argparse

# ================= Argument Parsing =================
parser = argparse.ArgumentParser()
parser.add_argument('--target_mb', type=int, default=500, help='Target file size in MB')
parser.add_argument('--fixed_int', type=str, default="100", help='Target file size in MB')
parser.add_argument('--num_unique_keys', type=int, default=100000, help='Number of Distinct Integers (from 0 to num_unique_keys-1)')
parser.add_argument('--filename', type=str, default="input_100+digit.txt", help='Output filename')
args = parser.parse_args()

FILENAME = args.filename
# The integer to repeat
FIXED_INT = args.fixed_int + " "

NUM_UNIQUE_KEYS = args.num_unique_keys

TARGET_SIZE_MB = args.target_mb
TARGET_SIZE_BYTES = TARGET_SIZE_MB * 1024 * 1024  # 104,857,600 bytes
SPLIT_POINT = TARGET_SIZE_BYTES // 2              # 52,428,800 bytes


# ================= Generate Data =================
def generate_data():
    print(f"Generating EXACTLY {TARGET_SIZE_MB}MB ({TARGET_SIZE_BYTES} bytes) of data...")
    print(f" - First {TARGET_SIZE_MB/2}MB: Repeated '{FIXED_INT.strip()}'")
    print(f" - Second {TARGET_SIZE_MB/2}MB: Random 1-{NUM_UNIQUE_KEYS}")
    start_time = time.time()
    
    current_bytes = 0
    
    # buffer (how many integers per line)
    ints_per_line = 10000 
    
    with open(FILENAME, "w", encoding="utf-8") as f:
        
        # === Step 1: Writing fixed integer ===
        print(f"--> Writing fixed integer '{FIXED_INT.strip()}'")
        
        while current_bytes < SPLIT_POINT:
            # generate a line (chunk) of data
            raw_chunk = FIXED_INT * ints_per_line
            chunk = raw_chunk[:-1] + "\n"
            chunk_len = len(chunk)
            
            # Check overflow
            if current_bytes + chunk_len > SPLIT_POINT:
                # If overflow, rewrite the remaining
                remaining = SPLIT_POINT - current_bytes
                f.write(chunk[:remaining])
                current_bytes += remaining
                break
            else:
                # otherwies write the whole chunk
                f.write(chunk)
                current_bytes += chunk_len
                
            # Print progress for every 5MB
            if current_bytes % (5 * 1024 * 1024) < chunk_len:
                progress = (current_bytes / TARGET_SIZE_BYTES) * 100
                print(f"\rStep 1: {progress:.2f}% ({current_bytes} / {TARGET_SIZE_BYTES} bytes)", end="")
        
        print(f"\n--> Step 2: Writing random integers within [1, {NUM_UNIQUE_KEYS}]...")
        
        # === Step 2: Writing random integers ===
        while current_bytes < TARGET_SIZE_BYTES:
            # generate a line (chunk) of data
            raw_chunk = [str(random.randint(0, NUM_UNIQUE_KEYS-1)) for _ in range(ints_per_line)]
            chunk = " ".join(raw_chunk) + "\n"
            chunk_len = len(chunk)
            
            # Check overflow
            if current_bytes + chunk_len > TARGET_SIZE_BYTES:
                # If overflow, rewrite the remaining
                remaining = TARGET_SIZE_BYTES - current_bytes
                f.write(chunk[:remaining])
                current_bytes += remaining
                break
            else:
                # Overwise, write the whole batch
                f.write(chunk)
                current_bytes += chunk_len
            
            # Print progress for every 5MB
            if current_bytes % (5 * 1024 * 1024) < chunk_len:
                progress = (current_bytes / TARGET_SIZE_BYTES) * 100
                print(f"\rStep 2: {progress:.2f}% ({current_bytes} / {TARGET_SIZE_BYTES} bytes)", end="")

    end_time = time.time()
    final_size = os.path.getsize(FILENAME)
    
    print(f"\n\nDone! File '{FILENAME}' created in {end_time - start_time:.2f} seconds.")
    print(f"Target Size: {TARGET_SIZE_BYTES} bytes")
    print(f"Actual Size: {final_size} bytes")
    
    if final_size == TARGET_SIZE_BYTES:
        print(f"✅ Size matches EXACTLY {TARGET_SIZE_MB}MB")
    else:
        print(f"❌ Size mismatch! Diff: {final_size - TARGET_SIZE_BYTES}")

if __name__ == "__main__":
    generate_data()
