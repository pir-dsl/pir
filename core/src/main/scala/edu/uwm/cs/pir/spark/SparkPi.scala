package edu.uwm.cs.pir.spark

import scala.math.random
import org.apache.spark._
import SparkContext._
import org.apache.log4j.PropertyConfigurator

import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.spark.SparkObject._

//import edu.uwm.cs.pir.spark.SparkObject

/** Computes an approximation to pi */
object SparkPi {
  def main(args: Array[String]) {
    
	  System.setProperty("spark.executor.memory", "256m")

    val spark = new SparkContext("local[4]", "SparkPi",
      System.getenv("/SPARK_HOME"), Seq("assembly/target/scala-2.9.3/pir-assembly-0.1.0.jar"))

    val slices = if (args.length > 0) args(0).toInt else 2
    val n = 100000 * slices
    val count = spark.parallelize(1 to n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y < 1) 1 else 0
    }.reduce(_ + _)
    log("Pi is roughly " + 4.0 * count / n)
    //System.exit(0)
  }
}