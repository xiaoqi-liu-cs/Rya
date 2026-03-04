package Rya

import scala.io.Source
import java.io.{File, PrintWriter}

// Since the String mapping is strictly done in Step0_Client_Data_Process,
// this loader simply assumes the input is a purely numerical Int array.
object DataLoader {

  // --- Load Int Array ---
  def loadIntData(fileName: String): (Array[Int], Array[Int], Int) = {
    println(Console.YELLOW + s"\n[DataLoader] Reading from $fileName ..." + Console.RESET)
    
    val file = new File(fileName)
    if (!file.exists()) {
      println(s"[DataLoader] Error: File '$fileName' not found.")
      return (Array.empty[Int], Array.empty[Int], 0)
    }

    val fileSource = Source.fromFile(file)
    val inputDataStrings = try {
      fileSource.getLines().mkString(" ")
        .split("[\\s,]+") 
        .filter(_.nonEmpty)
    } finally {
      fileSource.close()
    }
    
    // Return
    val inputDataInts = inputDataStrings.map(_.toInt)
    val globalKeys = inputDataInts.distinct.sorted
    val numUniqueKeys = globalKeys.length
    
    (inputDataInts, globalKeys, numUniqueKeys)
  }

  // ======================================================================================

  // --- Save RYA Output ---
  def saveOutput(histogram: Map[Int, Int], baseName: String): Unit = {
    val sortedHistogram = histogram.toSeq.sortBy(_._1)

    println("\n=============== Final Histogram Result ===============")
    println("(Printing first 10 items only...)")

    sortedHistogram.take(10).foreach { case (k, count) => println(f"($k, $count)") }
    if (sortedHistogram.length > 10) println("...")

    // Save
    val outputFile = new File(s"${baseName}_Rya_Output.txt")
    val writer = new PrintWriter(outputFile)
    try {
      sortedHistogram.foreach { case (k, count) => writer.println(f"($k, $count)") }
      println(Console.CYAN + s"\n[Output] Int histogram saved to: ${outputFile.getAbsolutePath}" + Console.RESET)
    } finally { writer.close() }
  }
}
