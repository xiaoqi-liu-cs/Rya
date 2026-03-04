# Choose which algorithm you want, or both
ALGOS=("bitonic" "bucket") 

# Choose which input file you want
FILES=(
    "identical_int.txt"
    "identical+random_int.txt"
    "random_int.txt"
)

echo "Start Rya Obliviousness Experiment..."

# =======================================================================

# For each oblivious sort algorithm, test every file

for ALGO in "${ALGOS[@]}"; do
    for FILE in "${FILES[@]}"; do
        if [ ! -f "$FILE" ]; then
            echo "Error: Couldn't find $FILE"
            continue
        fi
        
        BASENAME="${FILE%.*}"
        
        echo -e "=========================================================="
        echo "Filename = $FILE, ALGO=$ALGO"
        echo "=========================================================="
        
        sbt --error \
            -DSORT_ALGO="$ALGO" \
            -DBATCH_SIZE=numUniqueKeys \
            -DBASE_NAME="$BASENAME" \
            -DINPUT_FILE="$FILE" \
            "run"
    done
done

echo -e "\n=========================================================="
echo "All experiments finished!."
echo "=========================================================="
