package Rya

// Element used for Bin-and-Sum (OLABEL)
// label: Target bucket address calculated by the OLABEL algorithm
// key: Original key (used for final output; routing only looks at label)
// count: Currently aggregated count
case class BinItem(key: Int, label: Int, count: Int, isDummy: Boolean)

object BinItem {
  def createDummy(): BinItem = {
    // Used -1 to represent DUMMY
    BinItem(-1, -1, 0, isDummy = true) 
  }
}

// Used for Oblivious Bin Packing (OBIN).
// Corresponds to the tuple (g, priority) mentioned in the algorithm description.
case class ObinPacket(
      item: BinItem,
      var g: Int,           // destined bin number, for dummy element，we use Int.MaxValue to represent ⊥
      isFiller: Boolean,
      isDummy: Boolean,
      priority: Int,        // for break ties: Real > Filler > Dummy
      var offset: Int = 0,
      var tag: Int = 0      // 0: normal, 1: excess, 2: dummy_tag (make sure dummy will be at the end)
  ) {
    override def toString: String = {
      // keyStr is for print & visualization
      val keyStr = if (isFiller) "Filler" else if (isDummy) "Dummy" else item.key.toString
      val countStr = if (isFiller || isDummy) "-" else item.count.toString
      val tagStr = tag match { case 0 => "✔ Normal"; case 1 => "✖ Excess"; case 2 => "⊥ Dummy" }
      val gStr = if (g == Int.MaxValue) " ⊥" else f"$g%2d"
      f"[$keyStr%8s, $countStr%2s | g=$gStr | p=$priority%2d | off=$offset%d | $tagStr]"
    }
  }
  
  
// Element used ONLY in Oblivious Bucket Sort.
case class Element(value: Int, randomKey: Int, isDummy: Boolean)

object Element {
  def createReal(value: Int, maxBuckets: Int): Element = {
    val key = scala.util.Random.nextInt(maxBuckets)
    Element(value, key, isDummy = false)
  }

  def createDummy(): Element = {
    // Used -1 to represent DUMMY instead of "DUMMY"
    Element(-1, -1, isDummy = true)
  }
}
