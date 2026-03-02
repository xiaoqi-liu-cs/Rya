package oblivious_sort

// value: String
case class Element(value: String, randomKey: Int, isDummy: Boolean)

object Element {
  def createReal(value: String, maxBuckets: Int): Element = {
    val key = scala.util.Random.nextInt(maxBuckets)
    Element(value, key, isDummy = false)
  }

  def createDummy(): Element = {
    Element("DUMMY", -1, isDummy = true)
  }
}

// Element used for Bin-and-Sum (oLabel)
// label: Target bucket address calculated by the OLABEL algorithm
// key: Original key (used for final output; routing only looks at label)
// count: Currently aggregated count
case class BinItem(key: String, label: Int, count: Int, isDummy: Boolean)

object BinItem {
  def createDummy(): BinItem = BinItem("DUMMY", -1, 0, isDummy = true)
}


// External wrapper class for Oblivious Bin Packing (OBIN).
// Corresponds to the tuple (g, priority) mentioned in the algorithm description.
case class oBinPacket(
      item: BinItem,
      g: Int, // group
      isReal: Boolean,
      priority: Int,
      var offset: Int = 0, // Calculate the ranking in the group
      var tag: String = "normal" // "excess/normal" in the algorithm
  ){
    // Help to print the result during the oBin procedure
    override def toString: String = {
      val keyStr = if (isReal) item.key else "Filler"
      val countStr = if (isReal) item.count.toString else "-"
      val tagSymbol = if (tag == "normal") "✔" else "✖" // ✔=Normal, ✖=Excess
      f"[$keyStr, $countStr | g=$g%2d | p=$priority%2d | off=$offset%d | $tagSymbol]"
    }
  }
