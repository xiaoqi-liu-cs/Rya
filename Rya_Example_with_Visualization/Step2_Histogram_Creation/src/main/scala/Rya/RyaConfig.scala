package Rya

import java.io.File

case class RyaConfig(
fileName: String,
  baseName: String,
  batchSizeConf: String,
  algo: String
) {
  // We need to know numUniqueKeys (by loading file) first in order to calculate batchSize and num of batches.
  // Give some warning regarding BATCH_SIZE from build.sbt
  def getBatchSizeAndNumBatches(N: Int, numUniqueKeys: Int): (Int, Int) = {
    val batchSize = if (batchSizeConf == "numUniqueKeys") {
      println(s"[Config] Auto-set the batchSize to be the number of unique keys...")
      numUniqueKeys
    } else {
      try { batchSizeConf.toInt } catch {
        case _: NumberFormatException => 
          println(Console.RED + s"[Warning] Invalid BATCH_SIZE '$batchSizeConf', defaulting to numUniqueKeys = $numUniqueKeys." + Console.RESET)
          numUniqueKeys
      }
    }

    if (batchSize < 2) println(Console.RED + s"[Warning] batchSize=$batchSize is too small! Bin-and-Sum requires at least 2 items." + Console.RESET)
    else if (batchSize > N) println(Console.YELLOW + s"[Warning] batchSize=$batchSize is larger than input size ($N)." + Console.RESET)
    else if (batchSize < numUniqueKeys) println(Console.YELLOW + s"[Warning] batchSize=$batchSize is smaller than unique keys ($numUniqueKeys)." + Console.RESET)

    val numBatches = Math.ceil(N.toDouble / batchSize).toInt

    println(s"[Config] Splitting input into batches of size batchSize=$batchSize")
    println(s"[Config] Total Number of Batches: $numBatches") 
    println(s"[Config] Bin-and-Sum Bucket Capacity fixed to 2")
    println(Console.CYAN + s"[Config] Using Sorting Algorithm: ${algo.toUpperCase}" + Console.RESET)
    
    (batchSize, numBatches)
  }
}

object RyaConfig {
  // Load all the static arguments from build.sbt
  def load(): RyaConfig = {
    val baseName = sys.props.getOrElse("BASE_NAME", "Example")
    val fileName = sys.props.getOrElse("INPUT_FILE", "input.txt")
    
    val batchSizeConf = sys.props.getOrElse("BATCH_SIZE", "numUniqueKeys")
    
    val algo = sys.props.getOrElse("SORT_ALGO", "bucket")
    
    RyaConfig(fileName, baseName, batchSizeConf, algo)
  }
}
