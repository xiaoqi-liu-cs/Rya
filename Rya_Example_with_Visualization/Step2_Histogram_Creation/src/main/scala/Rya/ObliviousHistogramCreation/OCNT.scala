package Rya

object OCNT {

  // --- OCNT: Oblivious Counting & Routing ---
  def runOCNT(bucket0: Array[BinItem], bucket1: Array[BinItem], currentBitMask: Int): (Array[BinItem], Array[BinItem]) = {
    // Filter dummy elements
    val pool = bucket0.filter(!_.isDummy) ++ bucket1.filter(!_.isDummy)
    
    // Group by BOTH Label and Key to prevent merging distinct keys that accidentally collide.
    // Dummies (which usually have key="DUMMY" and count=0) will be grouped together.
    val aggregated = pool.groupBy(x => (x.label, x.key)).map { case ((lbl, k), items) =>
      val totalCount = items.map(_.count).sum
      
      // --- Print MERGE Info ---
      if (items.length > 1) {
        val countsStr = items.map(_.count).mkString("+")
        println(Console.CYAN + f"      [MERGE] Key '$k' (Label $lbl): $countsStr = $totalCount" + Console.RESET)
      }
      
      BinItem(k, lbl, totalCount, isDummy = false)
    }.toList

    val (goesTo0, goesTo1) = aggregated.partition(item => (item.label & currentBitMask) == 0)

    val CAPACITY = 2 
    def pad(items: List[BinItem]): Array[BinItem] = {
      val res = new Array[BinItem](CAPACITY)
      for (i <- 0 until CAPACITY) {
        if (i < items.size) res(i) = items(i)
        else res(i) = BinItem.createDummy()
      }
      res
    }

    (pad(goesTo0), pad(goesTo1))
  }
}
