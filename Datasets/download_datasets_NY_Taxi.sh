# Make sure raw_data directory exists
mkdir -p raw_data
cd raw_data

# Function to download a single month
download_month() {
    local y=$1
    local m=$2
    # Make sure the format of month (Month:1 -> 01)
    local m_formatted=$(printf "%02d" $((10#$m)))
    
    local url="https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_${y}-${m_formatted}.parquet"
    local file_name="yellow_tripdata_${y}-${m_formatted}.parquet"

    echo "Downloading: $file_name ..."
    # -q and --show-progress keeps the output clean but shows the bar
    wget -q --show-progress -c "$url" -O "$file_name"

    if [ $? -eq 0 ]; then
        echo -e "  -> Success: $file_name\n"
    else
        echo "  -> ERROR! File not found or download failed."
        rm -f "$file_name"
    fi
}

# Change the mode to be either (month, single_year, multi_year)
# mode=month: Download Dataset of a Single Month (${YEAR}-${MONTH})
# mode=single_year: Download Dataset of a Whole Year (${SINGLE_YEAR})
# mode=multi_year: Download Dataset of Multiple Years (${START_YEAR} to ${END_YEAR})
MODE=month

# mode=month:
YEAR=2020
MONTH=01

# mode=single_year
SINGLE_YEAR=2020

# mode=multi_year
START_YEAR=2021
END_YEAR=2025


echo "========================================"
echo "Start downloading NYC TLC Yellow Taxi Dataset"

if [ "$MODE" == "month" ]; then
    echo "Mode: Download Dataset of a Single Month (${YEAR}-${MONTH})"
    echo "========================================"
    download_month $YEAR $MONTH

elif [ "$MODE" == "single_year" ]; then
    echo "Mode: Download Dataset of a Whole Year (${SINGLE_YEAR})"
    echo "========================================"
    for m in {1..12}; do
        download_month $SINGLE_YEAR $m
    done

elif [ "$MODE" == "multi_year" ]; then
    echo "Mode: Download Dataset of Multiple Years (${START_YEAR} to ${END_YEAR})"
    echo "========================================"
    # Loop through the years using seq
    for y in $(seq $START_YEAR $END_YEAR); do
        echo "--- Fetching Year $y ---"
        for m in {1..12}; do
            download_month $y $m
        done
    done

else
    echo "Error! Change the mode to be either (month, single_year, multi_year)"
    echo "========================================"
    cd ..
    exit 1
fi

echo -e "\n================== ls -l yellow* ======================"
ls -l yellow*

cd ..
