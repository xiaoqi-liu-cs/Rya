package oblivious_sort

import scala.io.Source
import java.io.{File, PrintWriter} // Import PrintWriter

object Main {
  def main(args: Array[String]): Unit = {
    // Default to "input.txt" if not provided
    val fileName = sys.props.getOrElse("INPUT_FILE", "input.txt")
    
    if (!new File(fileName).exists()) {
      println(s"Error: File '$fileName' not found.")
      return
    }

    println(s"Reading from $fileName ...")
    val fileSource = Source.fromFile(fileName)
    val inputData = try {
      fileSource.getLines().mkString(" ")
        .split("[\\s,]+") // Split by space, comma, newline
        .filter(_.nonEmpty)
    } finally {
      fileSource.close()
    }

    if (inputData.isEmpty) return

    val N = inputData.length
    println(Console.CYAN + s"\n[Config] Input: Loaded $N items." + Console.RESET)
    
    
    val algo = sys.props.getOrElse("SORT_ALGO", "bucket").toLowerCase
    println(Console.CYAN + s"[Config] Using Sorting Algorithm: ${algo.toUpperCase}" + Console.RESET)


    val sortedData = OSORT.runOSORT(inputData)
  }
}
