package Rya

object OBIN {

  /**
   * 运行 Oblivious Bin Packing 算法
   * @param inputs 从 Bin-and-Sum 出来的稀疏结果 (computedCounts)
   * @param globalKeys 全局 Key 列表，定义了目标桶的顺序
   * @return 按照 globalKeys 顺序排列的计数值数组 (Array[Int])
   */
def runOBIN(inputs: Array[BinItem], globalKeys: Array[Int]): Array[Int] = {
    
    val Z = 1 // we can perform the bin-packing with O(1) sized bins, here Z=1
    val B = globalKeys.length
    val dummyItem = BinItem.createDummy()

    println(Console.YELLOW + f"\n    [OBIN] Starting Strict Oblivious Routing to $B slots (Capacity Z=$Z)..." + Console.RESET)

    // ==========================================
    // Step 0: Inputs
    // ==========================================
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
    val fillers = (0 until B).flatMap { g =>
      (0 until Z).map { _ =>
        // priority = -1
        ObinPacket(dummyItem, g = g, isFiller = true, isDummy = false, priority = -1)
      }
    }

    var workingArray = inPackets ++ fillers
    printDebugState("Step 1: Padded Array (Including Fillers)", workingArray)

    // We need a dummy sentinel for padding the Bitonic Sort internally
    val sentinelPacket = ObinPacket(dummyItem, Int.MaxValue, isFiller = false, isDummy = true, priority = -100)

    // ==========================================
    // Step 2: Obliviously sort by group number, placing all dummies at the end.
    // Sort by BitonicSortOBIN.scala
    // Higher priority in front, fillers after real elements.
    // ==========================================
    workingArray = BitonicSortOBIN.sortPackets(workingArray, sentinelPacket, (a, b) => {
      if (a.g != b.g) Integer.compare(a.g, b.g)
      else Integer.compare(b.priority, a.priority) // descending
    })
    printDebugState("Step 2: Sorted by Group (g) & Priority", workingArray)

    // ==========================================
    // 论文 Step 3: Oblivious propagation algorithm. Find leftmost in group.
    // Calculate offset. If offset > Z -> 'excess', else 'normal'.
    // ==========================================
    if (workingArray.length > 0) {
      // 遍历一次，模拟 oblivious scan
      for (i <- 0 until workingArray.length) {
        val curr = workingArray(i)
        
        if (curr.isDummy) {
          curr.tag = 2 // 特殊 tag，确保原有的 dummy 留在最后
        } else {
          // 判断是否是同组的继续
          if (i > 0 && curr.g == workingArray(i - 1).g && curr.g != Int.MaxValue) {
            curr.offset = workingArray(i - 1).offset + 1
          } else {
            curr.offset = 1 // 新组的首个元素
          }

          // 如果超载，标记为 excess (1)；否则 normal (0)
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
    // 论文 Step 5: Truncate resulting array. 
    // The first B * Z elements form Out, the rest form Remain.
    // ==========================================
    val outArray = workingArray.take(B * Z)
    // val remainArray = workingArray.drop(B * Z) // (Remain 数组在本实现中可直接丢弃，不影响直方图输出)
    println(Console.GREEN + f"      -> [Step 5] Truncating to first ${B * Z} items (Out Array)" + Console.RESET)

    // ==========================================
    // 论文 Step 6: Cleanup. If it is a filler, replace with a dummy.
    // Remove temporary tags. Extract final counts.
    // ==========================================
    val finalCounts = outArray.map { packet =>
      // 如果不是 Real 数据（即它是 Filler），它的计数值被抹平为 0
      if (!packet.isFiller && !packet.isDummy) packet.item.count
      else 0 
    }
    
    println(Console.YELLOW + f"    [OBIN] Routing Complete." + Console.RESET)
    finalCounts
  }

  // --- 辅助打印函数 ---
  private def printDebugState(stageName: String, arr: Array[ObinPacket]): Unit = {
    println(Console.CYAN + f"\n      --- $stageName ---" + Console.RESET)
    if (arr.isEmpty) return

    // 移除 shouldTruncate 逻辑，直接遍历整个 arr
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
