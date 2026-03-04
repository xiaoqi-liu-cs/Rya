package Rya

import scala.collection.mutable.ArrayBuffer

object BucketObliviousSort {
  def mergeSplit(bucket0: Seq[Element], bucket1: Seq[Element], Z: Int, currentBitMask: Int): (Seq[Element], Seq[Element]) = {
    val pool = bucket0 ++ bucket1
    val realElements = pool.filter(!_.isDummy)
    val (goesTo0, goesTo1) = realElements.partition(e => (e.randomKey & currentBitMask) == 0)

    // Ignore overflow printings in this strict version
    
    def fillToZ(seq: Seq[Element]): Seq[Element] = {
      if (seq.size >= Z) seq.take(Z)
      else seq ++ Seq.fill(Z - seq.size)(Element.createDummy())
    }
    (fillToZ(goesTo0), fillToZ(goesTo1))
  }

  // --- Main Sort Function ---
  def sort(input: Array[Int], Z: Int): Array[Int] = {
    val N = input.length
    
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

    for (level <- 0 until levels) {
      val currentBitMask = 1 << level
      val distance = 1 << level 
      val nextBuckets = new Array[Seq[Element]](B)
      val processed = new Array[Boolean](B)

      for (i <- 0 until B) {
        if (!processed(i)) {
           if ((i & distance) == 0) {
             val partner = i + distance
             if (partner < B) {
               val (out0, out1) = mergeSplit(buckets(i), buckets(partner), Z, currentBitMask)
               nextBuckets(i) = out0
               nextBuckets(partner) = out1
               processed(i) = true
               processed(partner) = true
             }
           }
        }
      }
      buckets = nextBuckets
    }

    // Final sorting step
    val flattened = buckets.flatten
    val realOnly = flattened.filter(!_.isDummy)
    val sortedArray = realOnly.map(_.value).toArray
    java.util.Arrays.parallelSort(sortedArray)
    sortedArray
  }
}
