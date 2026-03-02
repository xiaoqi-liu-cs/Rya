package oblivious_sort

object OSORT {

  // --- Oblivious Sort (2 options: bitonic/bucket) ---
  def runOSORT(Data: Array[String]): Array[String] = {
    val currentDataSize = Data.length // Use actual size
  
    val algo = sys.props.getOrElse("SORT_ALGO", "bucket").toLowerCase
    
    val sortedData = if (algo == "bitonic") {
      println(Console.YELLOW + f"  -> Bitonic Sort (O(N log^2 N))......" + Console.RESET)
      BitonicSort.sort(Data)
    } else {
      val sortZ = math.max(2, math.ceil(math.sqrt(currentDataSize)).toInt)
      println(Console.YELLOW + f"  -> Oblivious Bucket Sort (Z=$sortZ = sqrt($currentDataSize))......" + Console.RESET)
      BucketObliviousSort.sort(Data, sortZ)
    }
    
    sortedData
  }
}
