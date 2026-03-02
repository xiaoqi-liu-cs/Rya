package oblivious_sort

object BitonicSort {

  // Helper: Print current array state
  def printState(arr: Array[String], stage: Int, step: Int, swapCount: Int): Unit = {
    // Replace padded MAX_VAL with a visual marker for cleaner output
    val content = arr.map { s =>
      if (s == "\uFFFF") "[_]" else s 
    }.mkString(" ")
    
    println(f"    [Stage $stage, Step $step] Swaps: $swapCount%-3d | $content")
  }

  def sort(input: Array[String]): Array[String] = {
    val n = input.length
    
    // 1. Bitonic Sort requires N to be a power of 2
    var paddedN = 1
    while (paddedN < n) paddedN *= 2

    // 2. Padding with a max value sentinel
    val MAX_VAL = "\uFFFF" 
    val paddedInput = new Array[String](paddedN)
    Array.copy(input, 0, paddedInput, 0, n)
    for (i <- n until paddedN) paddedInput(i) = MAX_VAL

    println(s"    [Bitonic Config] Input N=$n, Padded to $paddedN")
    
    // 3. Bitonic Sort Core Logic (Iterative)
    // k: current sequence length being merged (2, 4, 8, ... N)
    var k = 2
    while (k <= paddedN) {
      // j: current comparison stride (k/2, k/4, ... 1)
      var j = k / 2
      while (j > 0) {
        var swapCount = 0
        for (i <- 0 until paddedN) {
          val l = i ^ j // find partner index
          if (l > i) {
            // Determine sort direction: 
            // ((i & k) == 0) means current block should be ascending
            val ascending = (i & k) == 0
            
            val keyI = paddedInput(i)
            val keyL = paddedInput(l)
            
            val cmp = keyI.compareTo(keyL)
            
            // Swap if necessary
            if ((ascending && cmp > 0) || (!ascending && cmp < 0)) {
              paddedInput(i) = keyL
              paddedInput(l) = keyI
              swapCount += 1
            }
          }
        }
        printState(paddedInput, k, j, swapCount)
        j /= 2
      }
      k *= 2
    }

    // 4. Remove padding and return
    paddedInput.take(n)
  }
}
