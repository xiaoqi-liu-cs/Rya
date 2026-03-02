package Rya

import scala.io.Source
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main {

  def main(args: Array[String]): Unit = {
    // ===============================================================================================
    // 0. Load All Static Configurations from build.sbt
    val config = RyaConfig.load()
    // ===============================================================================================
  
    // ===============================================================================================
    // 1. Read input file
    if (!new File(config.fileName).exists()) {
      println(s"Error: File '$config.fileName' not found.")
      return
    }
    
    println(s"Reading from $config.fileName ...")
    val fileSource = Source.fromFile(config.fileName)
    val inputDataStrings = try {
      fileSource.getLines().mkString(" ")
        .split("[\\s,]+") // Split by space, comma, newline
        .filter(_.nonEmpty)
    } finally {
      fileSource.close()
    }

    if (inputDataStrings.isEmpty) return

    val N = inputDataStrings.length
    println(Console.CYAN + s"\n[Config] Input: Loaded $N items." + Console.RESET)
    // ===============================================================================================
  
    // ===============================================================================================
    // 2. Load Data as Ints (Strictly requires numerical input mapped by Step0)
    val (inputDataInts, globalKeys, numUniqueKeys) = DataLoader.loadIntData(inputDataStrings)
    // ===============================================================================================

    // ===============================================================================================
    // 3. Get batchSize and numBatches before running Rya
    val (batchSize, numBatches) = config.getBatchSizeAndNumBatches(N, numUniqueKeys)
    // ===============================================================================================
    
    
    // Record the time duration of the program
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val startDateTime = LocalDateTime.now()
    println(Console.GREEN + s"\n>>> Program Started at: ${startDateTime.format(dateFormatter)}" + Console.RESET)

    // ===============================================================================================
    // 4. Start of time recording
    val startTime = System.nanoTime()

    // ===============================================================================================
    // Call RYA protocol
    val histogram = runRyaProtocol(inputDataInts, batchSize, globalKeys)
    // ===============================================================================================
    
    // End of time recording
    val endTime = System.nanoTime()
    val durationMs = (endTime - startTime) / 1e6
    val durationSec = durationMs / 1000.0
    println(Console.GREEN + s"\n>>> RYA Protocol Finished." + Console.RESET)
    println(Console.GREEN + f">>> Total Execution Time: $durationMs%.2f ms ($durationSec%.4f s)" + Console.RESET)
    // ===============================================================================================


    // ===============================================================================================
    // 5. Save RYA Output
    DataLoader.saveOutput(histogram, config.baseName)
    // ===============================================================================================
  }

  def runRyaProtocol(inputData: Array[Int], batchSize: Int, globalKeys: Array[Int]): Map[Int, Int] = {
    // Call RYA Protocol
    val histogram = OHIST.runOHIST(inputData, batchSize, globalKeys)
    
    histogram
  }
}
