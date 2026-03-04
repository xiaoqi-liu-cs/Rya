# Choose whether to generate the random strings, YES/NO
GENERATE_RANDOM_STRING="NO"

# Set the generated file size (MB)
SIZE_MB=50

# Set the random digit domain, from 0 to (NUM_UNIQUE_KEYS-1), for example 0-999 has 1000 in total
NUM_UNIQUE_KEYS=100000

# Set the identical/fixed integer you want to repeat for Obliviousness Test
FIXED_INT="10000"

# Set the generated FileName
FILENAME1="identical_int.txt"
FILENAME2="identical+random_int.txt"
FILENAME3="random_int.txt"
FILENAME4="random_string.txt"

echo -e "\n===================== Generate input files =====================\n"

echo "Running data generator..."
echo "Size: ${SIZE_MB} MB"
echo "Number of Unique Keys: ${NUM_UNIQUE_KEYS}"
echo "Integers are choosen from: [0, ${NUM_UNIQUE_KEYS}]"

echo -e "\n------------------------------ ${FILENAME1} ------------------------------\n"
python3 generate_identical_int.py --target_mb $SIZE_MB --num_unique_keys $NUM_UNIQUE_KEYS --fixed_int $FIXED_INT --filename $FILENAME1

echo -e "\n------------------------------ ${FILENAME2} ------------------------------\n"
python3 generate_identical+random_int.py  --target_mb $SIZE_MB --num_unique_keys $NUM_UNIQUE_KEYS --fixed_int $FIXED_INT  --filename $FILENAME2

echo -e "\n------------------------------ ${FILENAME3} ------------------------------\n"
python3 generate_random_int.py  --target_mb $SIZE_MB --num_unique_keys $NUM_UNIQUE_KEYS --filename $FILENAME3

echo -e "\n------------------------------ ${FILENAME4} ------------------------------\n"

if [ "$GENERATE_RANDOM_STRING" = "YES" ]; then
    echo "Generating ${FILENAME4} ..."
    python3 generate_random_string.py
else
    echo "Skipping random_string.txt generation."
fi

echo -e "\n-------------------------------------------------------------\n"

echo -e "\n===================== ls -l *.txt ====================="

echo "Show the file size, check whether they have the exactly the same size!"
ls -l *.txt

echo -e "\n===================== Show the number of items in each input file =====================\n"
LOG_FILE="countItems_log.txt"
# clear the old log
> $LOG_FILE

# Print the result and save to log at the same time 
python3 countItems.py $FILENAME1 | tee -a $LOG_FILE
python3 countItems.py $FILENAME2 | tee -a $LOG_FILE
python3 countItems.py $FILENAME3 | tee -a $LOG_FILE

if [ "$GENERATE_RANDOM_STRING" = "YES" ]; then
    python3 countItems.py $FILENAME4 | tee -a $LOG_FILE
fi

echo "Count results written to $LOG_FILE"
