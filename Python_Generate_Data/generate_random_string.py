import random
import string
import os
import time
import argparse

# ================= Argument Parsing =================
parser = argparse.ArgumentParser()
parser.add_argument('--target_mb', type=int, default=50, help='Target file size in MB')
parser.add_argument('--filename', type=str, default="random_string.txt", help='Output filename')
args = parser.parse_args()

FILENAME = args.filename
TARGET_SIZE_MB = args.target_mb
TARGET_SIZE_BYTES = TARGET_SIZE_MB * 1024 * 1024


# ================= Random Word Generator =================
def generate_word():
    length = random.randint(1, 5)
    return ''.join(random.choices(string.ascii_lowercase, k=length))


# ================= Generate Data =================
def generate_data():
    print(f"Generating EXACTLY {TARGET_SIZE_MB}MB ({TARGET_SIZE_BYTES} bytes) of random strings...")
    start_time = time.time()

    current_bytes = 0
    words_per_chunk = 10000

    with open(FILENAME, "w", encoding="utf-8") as f:
        while current_bytes < TARGET_SIZE_BYTES:
            # Generate chunk
            raw_chunk = [generate_word() for _ in range(words_per_chunk)]
            chunk = " ".join(raw_chunk) + "\n"
            chunk_len = len(chunk)

            # Check overflow
            if current_bytes + chunk_len > TARGET_SIZE_BYTES:
                remaining = TARGET_SIZE_BYTES - current_bytes
                f.write(chunk[:remaining])
                current_bytes += remaining
                break
            else:
                f.write(chunk)
                current_bytes += chunk_len

            # Print progress every 5MB
            if current_bytes % (5 * 1024 * 1024) < chunk_len:
                progress = (current_bytes / TARGET_SIZE_BYTES) * 100
                print(f"\rProgress: {progress:.2f}% ({current_bytes} / {TARGET_SIZE_BYTES} bytes)", end="")

    end_time = time.time()
    final_size = os.path.getsize(FILENAME)

    print(f"\n\nDone! File '{FILENAME}' created in {end_time - start_time:.2f} seconds.")
    print(f"Target Size: {TARGET_SIZE_BYTES} bytes")
    print(f"Actual Size: {final_size} bytes")

    if final_size == TARGET_SIZE_BYTES:
        print(f"✅ Size matches EXACTLY {TARGET_SIZE_MB}MB.")
    else:
        print(f"❌ Size mismatch! Diff: {final_size - TARGET_SIZE_BYTES} bytes")


if __name__ == "__main__":
    generate_data()
