package Rya

object OHIST {

  // --- Global Process ---
  def runOHIST(rawData: Array[Int], batchSize: Int, globalKeys: Array[Int], numBatches: Int): Map[Int, Int] = {
    // Identify the Universe of Keys (K) -> Done in Main now to avoid redundancy
    val numUniqueKeys = globalKeys.length
    
    // Initialize Global Histogram H (Algorithm 1 Output definition)
    // H is an array of size |K|
    val globalH = new Array[Int](numUniqueKeys)

    val batches = rawData.grouped(batchSize).zipWithIndex

    // For every Batch
    for ((batchData, idx) <- batches) {
      // Print dynamic progress
      val currentId = idx + 1
      print(f"\r[RYA] Processing batch ($currentId/$numBatches)")
    
      // Get local histogram for this batch
      val computedCounts = OBINSUM.processBatch(currentId, batchData, batchSize)
      
      // Apply OBIN, Call the external OBIN class
      // Map the computed counts to the global histogram format
      val localH_i = OBIN.runOBIN(computedCounts, globalKeys)

      // Sum local histogram to obtain global
      for (j <- 0 until numUniqueKeys) {
        globalH(j) += localH_i(j)
      }
    }
    
    // Convert back to Map for Main compatibility
    (globalKeys zip globalH).toMap
  }
}
