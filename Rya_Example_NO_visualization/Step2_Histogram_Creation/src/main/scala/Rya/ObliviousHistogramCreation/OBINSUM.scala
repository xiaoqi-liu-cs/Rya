package Rya

object OBINSUM {
  // --- Process Single Batch from OHIST ---
  def processBatch(batchId: Int, batchData: Array[Int], batchSize: Int): Array[Rya.BinItem] = {    
    // --- Step 1: Sort ---
    val sortedBatch = OSORT.runOSORT(batchData)
    
    // --- Step 2: OLABEL ---
    var B = 1
    while (B < sortedBatch.length) B *= 2
    if (B < 4) B = 4
    val levels = (math.log(B) / math.log(2)).toInt
    
    val labeledItems = OLABEL.runOLABEL(sortedBatch, B)
    
    // --- Step 3: OBINSUM ---
    
    var buckets = Array.fill(B)(Array.fill(2)(BinItem.createDummy()))
    for (i <- sortedBatch.indices) {
      buckets(i)(0) = labeledItems(i)
    }

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
      }

    // Extract items from bins
    // We only need the first element of each bin.
    val computedCounts = buckets.map(bin => bin(0))
    
    computedCounts
  }
 
}
