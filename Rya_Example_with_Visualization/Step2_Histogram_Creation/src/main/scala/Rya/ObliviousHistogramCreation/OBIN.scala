package Rya

object OBIN {

  /**
   * Oblivious Bin Packing, following the algorithm from https://eprint.iacr.org/2016/1084.pdf page 47
   * @param inputs: sparse result (computedCounts) from OBINSUM 
   * @param globalKeys: the canonical order of data
   * @return: Local histogram with CANONICAL order
   */
def runOBIN(inputs: Array[BinItem], globalKeys: Array[Int]): Array[Int] = {
    
    val Z = 1 // we can perform the bin-packing with O(1) sized bins, here Z=1
    val B = globalKeys.length

    println(Console.YELLOW + f"\n    [OBIN] Starting Strict Oblivious Routing to $B slots (Capacity Z=$Z)..." + Console.RESET)

    // ==========================================
    // Step 0: Inputs
    // ==========================================
    // For simplicity of this project (simulation), we use if-else here. For strict Oblivious project, we can change here.
    val inPackets = inputs.map { item =>
      if (item.isDummy) {
        // g = ⊥ (Int.MaxValue), priority = -2
        ObinPacket(item, g = Int.MaxValue, isFiller = false, isDummy = true, priority = -2)
      } else {
        // g = item.key, because all the item.keys are digits from 0 to numUniqueKeys
        // priority = 1
        ObinPacket(item, g = item.key, isFiller = false, isDummy = false, priority = 1)
      }
    }

    // ==========================================
    // Step 1: Append Z filler elements for each group
    // ==========================================
    val dummyItem = BinItem.createDummy()
    val fillers = (0 until B).flatMap { g =>
      (0 until Z).map { _ =>
        // priority = -1
        ObinPacket(dummyItem, g = g, isFiller = true, isDummy = false, priority = -1)
      }
    }

    var workingArray = inPackets ++ fillers
    printDebugState("Step 1: Padded Array (Including Fillers)", workingArray)

    // ==========================================
    // Step 2: Obliviously sort by group number, placing all dummies at the end.
    // Sort by BitonicSortOBIN.scala
    // Higher priority in front, fillers after real elements.
    // ==========================================
    // We need some dummy sentinel for padding the Bitonic Sort internally if the number of elements now is not 2^n
    val sentinelPacket = ObinPacket(dummyItem, Int.MaxValue, isFiller = false, isDummy = true, priority = -100)
    sentinelPacket.tag = 2

    // For simplicity of this project (simulation), we use if-else here. For strict Oblivious project, we can change here.
    workingArray = BitonicSortOBIN.sortPackets(workingArray, sentinelPacket, (a, b) => {
      if (a.g != b.g) Integer.compare(a.g, b.g)
      else Integer.compare(b.priority, a.priority) // descending
    })
    printDebugState("Step 2: Sorted by Group (g) & Priority", workingArray)

    // ==========================================
    // Step 3: Oblivious propagation algorithm. Find leftmost in group.
    // Calculate offset. If offset > Z -> 'excess', else 'normal'.
    // ==========================================
    if (workingArray.length > 0) {
      // simulate oblivious scan
      for (i <- 0 until workingArray.length) {
        val curr = workingArray(i)
        
        // For simplicity of this project (simulation), we use if-else here. For strict Oblivious project, we can change here.
        if (curr.isDummy) {
          curr.tag = 2 // make sure the dummies move to the end
        } else {
          // Check whether the two adjacent elements are in the same group
          if (i > 0 && curr.g == workingArray(i - 1).g && curr.g != Int.MaxValue) {
            curr.offset = workingArray(i - 1).offset + 1
          } else {
            curr.offset = 1 // The first element in the group
          }

          // curr.offset > Z, set to "excess" (1), otherwiese "normal" (0)
          if (curr.offset > Z) curr.tag = 1
          else curr.tag = 0
        }
      }
    }
    printDebugState("Step 3: After Propagation & Tagging", workingArray)

    // ==========================================
    // Step 4: Oblivious sort placing 'excess' and all 'dummies' at the end.
    // Sort by BitonicSortOBIN.scala
    // 'excess' should appear before 'dummies'.
    // ==========================================
    workingArray = BitonicSortOBIN.sortPackets(workingArray, sentinelPacket, (a, b) => {
      if (a.tag != b.tag) Integer.compare(a.tag, b.tag)
      else Integer.compare(a.g, b.g)
    })
    printDebugState("Step 4: Sorted by Tag (Normal -> Excess -> Dummy)", workingArray)

    // ==========================================
    // Step 5: Truncate resulting array. 
    // The first B * Z elements form Out, the rest form Remain.
    // ==========================================
    val outArray = workingArray.take(B * Z)
    println(Console.GREEN + f"      -> [Step 5] Truncating to first B * Z = ${B * Z} items (Out Array)" + Console.RESET)

    // ==========================================
    // Step 6: Cleanup. If it is a filler, replace with a dummy.
    // Remove temporary tags. Extract final counts.
    // ==========================================
    val finalCounts = outArray.map { packet =>
      // Only get the count of every element, it is already sorted in the canonical order by OBIN
      packet.item.count
    }
    
    println(Console.YELLOW + f"    [OBIN] Routing Complete." + Console.RESET)
    finalCounts
  }

  // --- help printer for visulization ---
  private def printDebugState(stageName: String, arr: Array[ObinPacket]): Unit = {
    println(Console.CYAN + f"\n      --- $stageName ---" + Console.RESET)
    if (arr.isEmpty) return

    // loop for the arr
    arr.zipWithIndex.foreach { case (p, idx) =>
      val color = p.tag match {
        case 0 => Console.GREEN  // Normal
        case 1 => Console.RED    // Excess
        case 2 => Console.WHITE  // Dummy
      }
      println(f"      [$idx%3d] $color$p${Console.RESET}")
    }
    println("      " + "-" * 55)
  }
}
