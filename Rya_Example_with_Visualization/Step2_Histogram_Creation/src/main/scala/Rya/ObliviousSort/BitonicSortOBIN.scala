package Rya

object BitonicSortOBIN {
  def sortPackets(input: Array[ObinPacket], dummySentinel: ObinPacket, cmp: (ObinPacket, ObinPacket) => Int): Array[ObinPacket] = {
    val n = input.length
    var paddedN = 1
    while (paddedN < n) paddedN *= 2

    val paddedInput = new Array[ObinPacket](paddedN)
    Array.copy(input, 0, paddedInput, 0, n)
    for (i <- n until paddedN) paddedInput(i) = dummySentinel

    var k = 2
    while (k <= paddedN) {
      var j = k / 2
      while (j > 0) {
        for (i <- 0 until paddedN) {
          val l = i ^ j 
          if (l > i) {
            val ascending = (i & k) == 0
            val keyI = paddedInput(i)
            val keyL = paddedInput(l)
            val comparison = cmp(keyI, keyL)
            
            if ((ascending && comparison > 0) || (!ascending && comparison < 0)) {
              paddedInput(i) = keyL
              paddedInput(l) = keyI
            }
          }
        }
        j /= 2
      }
      k *= 2
    }
    paddedInput.take(n)
  }
}
