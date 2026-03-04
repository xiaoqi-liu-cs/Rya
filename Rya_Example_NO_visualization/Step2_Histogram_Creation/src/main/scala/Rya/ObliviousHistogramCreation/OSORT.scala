package Rya

object OSORT {

  // --- Oblivious Sort (2 options: bitonic/bucket) ---
  def runOSORT(Data: Array[Int]): Array[Int] = {
    val currentDataSize = Data.length // Use actual size
  
    val algo = sys.props.getOrElse("SORT_ALGO", "bucket").toLowerCase
    
    val sortedData = if (algo == "bitonic") {
      BitonicSort.sort(Data)
    } else {
      val sortZ = math.max(2, math.ceil(math.sqrt(currentDataSize)).toInt)
      BucketObliviousSort.sort(Data, sortZ)
    }
    
    sortedData
  }
}
