package edu.uwm.cs.pir.misc

import java.io._
import edu.uwm.cs.mir.prototypes.feature._

object Utils {

  val formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  def time[A](block: => A)(algorithm: String, storeResult: Boolean = false): A = {
    val now = System.currentTimeMillis
    val result = block
    if (storeResult) {
      val micros = (System.currentTimeMillis - now)
      val filenameHeader = if (!algorithm.contains("(")) algorithm
      else algorithm.substring(0, algorithm.indexOf("("))
      val writer = new PrintWriter(
        new File(
          "output/" + filenameHeader + "-" +
            formatter.format(new java.util.Date(System.currentTimeMillis)) + ".txt"))
      writer.write("Execution Time: %d microseconds\n".format(micros))
      writer.close()
    }
    result
  }

  def chunkList[In <: IFeature](list: List[In], chunkSize: Int): List[List[In]] = {
    if (list.size <= chunkSize) List(list)
    else {
      val (head, tail) = list.splitAt(chunkSize)
      head :: chunkList(tail, chunkSize)
    }
  }

  implicit def isDebug = true

  def log(msg: String)(implicit level: String = "DEBUG", isDebug: Boolean = false): Unit = {
    if (("INFO" == level) || (isDebug)) {
      println(level + ": " + msg);
    }
  }
}
