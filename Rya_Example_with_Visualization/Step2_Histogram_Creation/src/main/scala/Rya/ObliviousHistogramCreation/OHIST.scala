package Rya

object OHIST {

  // --- Global Process ---
  def runOHIST(rawData: Array[Int], batchSize: Int, globalKeys: Array[Int]): Map[Int, Int] = {
    // Identify the Universe of Keys (K) -> Done in Main now to avoid redundancy
    val numUniqueKeys = globalKeys.length
    
    // Initialize Global Histogram H (Algorithm 1 Output definition)
    // H is an array of size |K|
    val globalH = new Array[Int](numUniqueKeys)

    val batches = rawData.grouped(batchSize).zipWithIndex

    // For every Batch
    for ((batchData, idx) <- batches) {
      // Get local histogram for this batch
      val computedCounts = OBINSUM.processBatch(idx + 1, batchData, batchSize)
      
      // Apply OBIN, Call the external OBIN class
      // Map the computed counts to the global histogram format
      println(Console.YELLOW + f"  -> Step 3: OBIN Routing (Routing ${computedCounts.count(!_.isDummy)} items to ${globalKeys.length} slots)......" + Console.RESET)
      val localH_i = OBIN.runOBIN(computedCounts, globalKeys)

      // Print Local Histogram for this batch
      println(f"\n  [Batch #$idx Local Histogram]")
      for (j <- globalKeys.indices) {
        println(f"  (${globalKeys(j)}, ${localH_i(j)})")
      }
      
      // Sum local histogram to obtain global
      for (j <- 0 until numUniqueKeys) {
        globalH(j) += localH_i(j)
      }
    }

    // Convert back to Map for Main compatibility
    (globalKeys zip globalH).toMap
  }
}
