echo "=========================================================="
echo "Initialize DAMON configurations..."
echo "=========================================================="
# Remove the limitation of Linux Perf
sudo sysctl -w kernel.perf_event_max_sample_rate=100000
sudo sysctl -w kernel.perf_cpu_time_max_percent=0
sudo sysctl -w kernel.perf_event_mlock_kb=2048

# Create output directory for DAMON results
mkdir -p DAMON_Result

# Choose which algorithm you want, or both
ALGOS=("bitonic") 

# Choose which input file you want
FILES=(
    "identical_int.txt"
    "identical+random_int.txt"
    "random_int.txt"
)

echo "=========================================================="
echo "Start Rya Obliviousness Experiment using DAMON recording..."
echo "=========================================================="

# =======================================================================

# For each oblivious sort algorithm, test every file

for ALGO in "${ALGOS[@]}"; do
    for FILE in "${FILES[@]}"; do
        if [ ! -f "$FILE" ]; then
            echo "Error: Couldn't find $FILE"
            continue
        fi
        
        BASENAME="${FILE%.*}"
        # Damon result name
        DATA_FILE="DAMON_Result/${BASENAME}_${ALGO}.data"
        HEATMAP_IMG="DAMON_Result/heatmap_${BASENAME}_${ALGO}.png"
        
        echo -e "=========================================================="
        echo "Filename = $FILE, ALGO=$ALGO"
        echo "=========================================================="
        
        sudo damo record -s 1000 -a 10000 -u 100000 -o "$DATA_FILE" -- "sbt --error \
            -DSORT_ALGO="$ALGO" \
            -DBATCH_SIZE=numUniqueKeys \
            -DBASE_NAME="$BASENAME" \
            -DINPUT_FILE="$FILE" \
            "run""
            
        echo "Generating DAMON reports for $BASENAME ($ALGO)..."
        sudo damo report heatmap --input "$DATA_FILE" --output "$HEATMAP_IMG" --resol 500 500
        echo "Done with $FILE using $ALGO."
    done
done

echo -e "\n=========================================================="
echo "All experiments finished!."
echo "=========================================================="




