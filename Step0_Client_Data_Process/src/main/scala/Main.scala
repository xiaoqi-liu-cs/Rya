package ClientDataProcess

import scala.io.Source
import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable

object Main {

  def main(args: Array[String]): Unit = {
    val mode = sys.props.getOrElse("MODE", "PreProcess")
    
    println(Console.GREEN + s"\n=== Running ProcessData in $mode Mode ===\n" + Console.RESET)

    // =================================================================================================
    // 1. Start of time recording
    val startTime = System.nanoTime()

    // =================================================================================================
    // 2. Run PreProcess or AfterProcess
    if (mode.equalsIgnoreCase("PreProcess")) {
      runPreProcess()
    } else if (mode.equalsIgnoreCase("AfterProcess")) {
      runAfterProcess()
    } else {
      println(Console.RED + s"Unknown MODE: $mode." + Console.RESET)
    }
    // =================================================================================================
 
    // 3. End of time recording
    val endTime = System.nanoTime()
    val durationMs = (endTime - startTime) / 1e6
    val durationSec = durationMs / 1000.0
    println(Console.GREEN + s"\n>>> $mode Mode Finished." + Console.RESET)
    println(Console.GREEN + f">>> Total Execution Time: $durationMs%.2f ms ($durationSec%.4f s)" + Console.RESET)
  }



  // =================================================================================================



  def runPreProcess(): Unit = {
    // ------------ Initialization ------------
    val baseName = sys.props.getOrElse("BASE_NAME", "Example")
    val inputFile = sys.props.getOrElse("INPUT_FILE", s"${baseName}.csv")
    
    if (!new File(inputFile).exists()) {
      println(Console.RED + s"Error: Input file '$inputFile' not found." + Console.RESET)
      return
    }
    println(s"Reading raw string data from $inputFile and Preprocessing ...")
    
    val stringToIntMap = mutable.HashMap.empty[String, Int] // Initialize map
    var currentId = 0
    val intsBuilder = mutable.ArrayBuilder.make[Int]()

    val fileSource = Source.fromFile(inputFile)
    try {
      // process the data line by line
      for (line <- fileSource.getLines()) {
        val tokens = line.split("[\\s,]+").filter(_.nonEmpty)
        for (token <- tokens) {
          // getOrElseUpdate: If the string is new, then assign it with a new ID, otherwise retreive.
          val id = stringToIntMap.getOrElseUpdate(token, {
            val newId = currentId
            currentId += 1
            newId
          })
          intsBuilder += id
        }
      }
    } finally { fileSource.close() }

    val inputDataInts = intsBuilder.result()
    val numItems = inputDataInts.length

    println(s"Loaded $numItems items. Total unique keys: $currentId")

    // ------------ Output Processed Data ------------

    val outIntFile = new File(s"${baseName}_Rya_Input.txt")
    val outIntWriter = new PrintWriter(outIntFile)
    try { outIntWriter.println(inputDataInts.mkString(" ")) } finally { outIntWriter.close() }
    println(Console.CYAN + s"[PreProcess] Saved Int array to ${outIntFile.getAbsolutePath}\n" + Console.RESET)

    val mappingFileStr = sys.props.getOrElse("MAPPING_FILE", s"${baseName}_Rya_Mapping.csv")
    val mappingFile = new File(mappingFileStr)
    val mappingWriter = new PrintWriter(mappingFile)
    try {
      mappingWriter.println("IntKey,StringValue")
      // write the map into mapping.csv
      stringToIntMap.foreach { case (str, id) => mappingWriter.println(s"$id,$str") }
      println(Console.CYAN + s"[PreProcess] Saved mapping dictionary to ${mappingFile.getAbsolutePath}\n" + Console.RESET)
    } finally { mappingWriter.close() }
  }


  // =================================================================================================
  
  
  def runAfterProcess(): Unit = {
    // ------------ Initialization ------------
    val baseName = sys.props.getOrElse("BASE_NAME", "Example")
    val outputFileStr = sys.props.getOrElse("OUTPUT_INT_FILE", s"${baseName}_Rya_Output.txt")
    val mappingFileStr = sys.props.getOrElse("MAPPING_FILE", s"${baseName}_Rya_Mapping.csv")

    if (!new File(outputFileStr).exists()) {
      println(Console.RED + s"Error: Output Int file '$outputFileStr' not found." + Console.RESET)
      return
    }
    if (!new File(mappingFileStr).exists()) {
      println(Console.RED + s"Error: Mapping file '$mappingFileStr' not found. Run PreProcess first!" + Console.RESET)
      return
    }

    println(s"Loading mapping dictionary from $mappingFileStr ...\n")
    val mapSource = Source.fromFile(mappingFileStr)
    val intToStringMap = try {
      mapSource.getLines().drop(1).map { line =>
        val parts = line.split(",")
        (parts(0).toInt, parts(1))
      }.toMap
    } finally { mapSource.close() }

    println(s"Reading $outputFileStr ...\n")
    val outSource = Source.fromFile(outputFileStr)
    // Reading data
    val pattern = """\((\d+),\s*(\d+)\)""".r
    
    // ------------ Process Data ------------
    
    val stringHistogram = try {
      outSource.getLines().flatMap {
        case pattern(keyStr, countStr) =>
          val keyInt = keyStr.toInt
          Some((intToStringMap.getOrElse(keyInt, keyInt.toString), countStr.toInt))
        case _ => None
      }.toList
    } finally { outSource.close() }

    // Sort by K, since K << N, so this step is fast
    val sortedStringHistogram = stringHistogram.sortBy(_._1)

    println("\n(Printing first 10 items only...)")
    sortedStringHistogram.take(10).foreach { case (str, count) => println(f"($str, $count)") }
    if (sortedStringHistogram.length > 10) println("...")

    // ------------ Output Processed Data ------------

    val finalOutputFile = new File(s"${baseName}_Rya_Output_Final.txt")
    val finalWriter = new PrintWriter(finalOutputFile)
    try {
      sortedStringHistogram.foreach { case (str, count) => finalWriter.println(f"($str, $count)") }
      println(Console.CYAN + s"[AfterProcess] Successfully saved String histogram to ${finalOutputFile.getAbsolutePath}" + Console.RESET)
    } finally { finalWriter.close() }
  }
}
