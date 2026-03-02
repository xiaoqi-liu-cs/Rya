package Rya

object HelpPrinter {

  // Helper: Convert to binary string, pad with leading zeros
  def toBinaryStr(n: Int, bits: Int): String = {
    val raw = n.toBinaryString
    "0" * (bits - raw.length) + raw
  }
 
  // Help Printer: Print the bins to visualize
  def printBinBuckets(buckets: Array[Array[BinItem]], stage: String, B: Int): Unit = {
    println(s"\n    --- Status: $stage ---")
    val levels = (math.log(B) / math.log(2)).toInt
    val dummyStr = "[      -      ]"
    
    var maxRealLen = 0
    for (bucket <- buckets; item <- bucket if !item.isDummy) {
      val binLbl = toBinaryStr(item.label, levels)
      val str = f"(${item.key}, ${item.count})(L:$binLbl)"
      if (str.length > maxRealLen) maxRealLen = str.length
    }
    val colWidth = math.max(maxRealLen, dummyStr.length)

    for (i <- buckets.indices) {
      val content = buckets(i).map { item =>
        val rawStr = if (item.isDummy) dummyStr 
        else {
          val binLbl = toBinaryStr(item.label, levels)
          f"(${item.key}, ${item.count})(L:$binLbl)"
        }
        rawStr + " " * (colWidth - rawStr.length)
      }.mkString(", ")
      println(f"    Bin $i%2d: $content")
    }
    println("    " + "-" * 60)
  }
 
}
