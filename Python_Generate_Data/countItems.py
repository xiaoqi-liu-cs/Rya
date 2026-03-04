import argparse

def analyze_items(file_path):
    unique_items = set()
    total_count = 0
    
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            items_in_line = line.strip().split()
            total_count += len(items_in_line)
            unique_items.update(items_in_line)
                
        return total_count, len(unique_items)
        


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("filename", help="Input file to analyze")
    args = parser.parse_args()

    total, distinct = analyze_items(args.filename)

    print("For the file:", args.filename)
    print(f"Total items:    {total}")
    print(f"Distinct items: {distinct}\n")
