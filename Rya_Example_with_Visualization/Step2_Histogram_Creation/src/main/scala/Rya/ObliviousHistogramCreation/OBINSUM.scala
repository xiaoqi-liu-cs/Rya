package Rya

object OBINSUM {
  // --- Process Single Batch from OHIST ---
  def processBatch(batchId: Int, batchData: Array[Int], batchSize: Int): Array[Rya.BinItem] = {
    println(Console.MAGENTA + f"\n\n===================== Processing Batch #$batchId (Size: $batchSize) =====================" + Console.RESET)
    
    // --- Step 1: Sort ---
    val sortedBatch = OSORT.runOSORT(batchData)
    
    println(f"\n  [Batch #$batchId Result] Sorted Batch: ${sortedBatch.mkString(", ")}")
    println("\n\n\n") 

    // --- Step 2: OLABEL ---
    var B = 1
    while (B < sortedBatch.length) B *= 2
    if (B < 4) B = 4
    val levels = (math.log(B) / math.log(2)).toInt
    
    println(Console.YELLOW + f"  -> Step 2: Bin-and-Sum (B=$B, Levels=$levels, Capacity=2)......" + Console.RESET)

    val labeledItems = OLABEL.runOLABEL(sortedBatch, B)
    
    // --- Step 3: OBINSUM ---
    
    var buckets = Array.fill(B)(Array.fill(2)(BinItem.createDummy()))
    for (i <- sortedBatch.indices) {
      buckets(i)(0) = labeledItems(i)
    }

    HelpPrinter.printBinBuckets(buckets, "Batch Initialization", B)

    for (level <- 0 until levels) {
      val currentBitMask = 1 << level
      val distance = 1 << level
      val nextBuckets = new Array[Array[BinItem]](B)
      
      for (i <- 0 until B) {
        if ((i & distance) == 0) {
          val partner = i + distance
          val (out0, out1) = OCNT.runOCNT(buckets(i), buckets(partner), currentBitMask)
          nextBuckets(i) = out0
          nextBuckets(partner) = out1
        }
      }
      buckets = nextBuckets
      HelpPrinter.printBinBuckets(buckets, s"End of Level $level (Mask ${HelpPrinter.toBinaryStr(currentBitMask, levels)})", B)
    }

    // Extract items from bins
    // We only need the first element of each bin.
    val computedCounts = buckets.map(bin => bin(0))
    
    computedCounts
  }
 
}
