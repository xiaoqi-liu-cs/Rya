package Rya

object OLABEL {

  // --- Oblivious Labeling ---
  def runOLABEL(sortedKeys: Array[Int], B: Int): Array[BinItem] = {
  
    val n = sortedKeys.length
    val labeled = new Array[BinItem](n)

    if (n > 0) {
      labeled(0) = BinItem(sortedKeys(0), 0, 1, isDummy = false)
    }

    for (i <- 1 until n) {
      val currentKey = sortedKeys(i)
      val prevKey = sortedKeys(i - 1)
      
      val label = if (currentKey == prevKey) {
        labeled(i - 1).label
      } else {
        // The Butterfly network routing (oCnt) checks LSB (Bit 0) at Level 0.
        // Level 0 pairs indices (i, i+1). These differ in Bit 0.
        // Using 'i' ensures their labels differ in Bit 0, causing them to split immediately.
        i
      }
      labeled(i) = BinItem(currentKey, label, 1, isDummy = false)
    }
    labeled
  }

 
}
