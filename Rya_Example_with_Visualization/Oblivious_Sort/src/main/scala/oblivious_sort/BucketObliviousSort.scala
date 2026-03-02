package oblivious_sort

import scala.collection.mutable.ArrayBuffer

object BucketObliviousSort {

  // --- Formatting Print Function ---
  def elementToString(e: Element, showBinary: Boolean, bitLength: Int, maxValLen: Int): String = {
    val dummyStr = "[   -   ]"
    val content = if (e.isDummy) {
      dummyStr
    } else {
      val keyStr = if (showBinary) {
        val bin = e.randomKey.toBinaryString
        "0" * (bitLength - bin.length) + bin
      } else {
        e.randomKey.toString
      }
      f"${e.value}%s(k:$keyStr)"
    }
    val realMaxLen = maxValLen + 3 + bitLength + 1
    val targetWidth = math.max(realMaxLen, dummyStr.length)
    content + " " * (targetWidth - content.length)
  }

  def printBuckets(buckets: Array[Seq[Element]], stageName: String, showBinary: Boolean, bitLength: Int, maxValLen: Int): Unit = {
    println(s"\n--- Status: $stageName ---")
    for (i <- buckets.indices) {
      val content = buckets(i).map(e => elementToString(e, showBinary, bitLength, maxValLen)).mkString(", ")
      println(f"Bucket $i%2d: $content")
    }
    println("-" * 80)
  }

  def mergeSplit(bucket0: Seq[Element], bucket1: Seq[Element], Z: Int, currentBitMask: Int, bucketIdx0: Int, bucketIdx1: Int): (Seq[Element], Seq[Element]) = {
    val pool = bucket0 ++ bucket1
    val realElements = pool.filter(!_.isDummy)
    val (goesTo0, goesTo1) = realElements.partition(e => (e.randomKey & currentBitMask) == 0)

    if (goesTo0.size > Z) {
      val dropped = goesTo0.drop(Z)
      println(Console.RED + f"!!! OVERFLOW merging $bucketIdx0 & $bucketIdx1 !!! Target $bucketIdx0 needs ${goesTo0.size}")
      println(f"  -> DROPPING: ${dropped.map(_.value).mkString(", ")}" + Console.RESET)
    }
    if (goesTo1.size > Z) {
      val dropped = goesTo1.drop(Z)
      println(Console.RED + f"!!! OVERFLOW merging $bucketIdx0 & $bucketIdx1 !!! Target $bucketIdx1 needs ${goesTo1.size}")
      println(f"  -> DROPPING: ${dropped.map(_.value).mkString(", ")}" + Console.RESET)
    }

    def fillToZ(seq: Seq[Element]): Seq[Element] = {
      if (seq.size >= Z) seq.take(Z)
      else seq ++ Seq.fill(Z - seq.size)(Element.createDummy())
    }
    (fillToZ(goesTo0), fillToZ(goesTo1))
  }

  // --- Main Sort Function ---
  def sort(input: Array[String], Z: Int): Array[String] = {
    val N = input.length
    val maxValLen = if (input.isEmpty) 0 else input.map(_.length).max
    
    var B = 1
    while (B * Z < 2 * N) { B *= 2 }
    val levels = (math.log(B) / math.log(2)).toInt

    var buckets: Array[Seq[Element]] = Array.fill(B)(Seq.empty)
    val allElements = input.map(v => Element.createReal(v, B))
    val tempBuckets = Array.fill(B)(new ArrayBuffer[Element]())
    
    for (i <- allElements.indices) {
      val targetBucketIdx = i % B
      if (tempBuckets(targetBucketIdx).size < Z) tempBuckets(targetBucketIdx) += allElements(i)
    }
    for (i <- 0 until B) {
      while (tempBuckets(i).size < Z) tempBuckets(i) += Element.createDummy()
      buckets(i) = tempBuckets(i).toSeq
    }

    printBuckets(buckets, "Initialization Completed", showBinary = true, bitLength = levels, maxValLen)

    for (level <- 0 until levels) {
      val currentBitMask = 1 << level
      println(s"\n>>> Processing Level $level (Checking Bit Mask: ${currentBitMask.toBinaryString}) <<<")
      val distance = 1 << level 
      val nextBuckets = new Array[Seq[Element]](B)
      val processed = new Array[Boolean](B)
      var pairCount = 0

      for (i <- 0 until B) {
        if (!processed(i)) {
           if ((i & distance) == 0) {
             val partner = i + distance
             if (partner < B) {
               print(f"    Bucket $i%2d <--> Bucket $partner%2d  |  ")
               pairCount += 1
               if (pairCount % 4 == 0) println()

               val (out0, out1) = mergeSplit(buckets(i), buckets(partner), Z, currentBitMask, i, partner)
               nextBuckets(i) = out0
               nextBuckets(partner) = out1
               processed(i) = true
               processed(partner) = true
             }
           }
        }
      }
      if (pairCount % 4 != 0) println()
      buckets = nextBuckets
      val useBinary = if (level == levels - 1) false else true
      printBuckets(buckets, s"End of Level $level", showBinary = useBinary, bitLength = levels, maxValLen)
    }

    println("\n--- Final Sorting ---")
    val flattened = buckets.flatten
    val realOnly = flattened.filter(!_.isDummy)
    val sortedArray = realOnly.map(_.value).toArray
    java.util.Arrays.parallelSort(sortedArray)
    sortedArray
  }
}
