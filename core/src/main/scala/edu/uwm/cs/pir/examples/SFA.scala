package edu.uwm.cs.pir.examples

import common._
import com.typesafe.config._

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.index._

import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.misc.Constants._
import edu.uwm.cs.pir.misc.InputType
import edu.uwm.cs.pir.compile.Compile._
import edu.uwm.cs.pir.compile.Function._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.compile._
import edu.uwm.cs.pir.spark.SparkObject._

object SFA {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      usage
    } else {
      
      sparkContext = initSparkConf
      val env = args(0)
      
      if ("se" == env) {
        time(sequentialSFA()) {
          "sequentialSFA"
        }
      } else if ("p" == env) {
        time(parallelSFA(4)) {
          "parallelSFA (4 processors)"
        }
      } else if ("sp" == env) {
        time(sparkSFA()) {
          "sparkSFA"
        }
        log("Complete sparkSFA")("INFO")
        //    time(sparkSFA()) {
        //      "jobVisit"
        //    }
      } else {
        usage
      }
      sparkContext.stop
      log("SFA mission complete")("INFO")
    }
  }

  def usage: Unit = {
    println("USAGE: SFA \"se/p/sp 1/2 env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("if in Spark execution, 1 to run program against sample, 2 against WikiPedia dataset)")
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialSFA(): Unit = {
    val s = new SequentialStrategy()
    val q = getQ(s)
  }

  def parallelSFA(numProcessors: Int): Unit = {
    val s = new ParallelStrategy(numProcessors)
    val q = getQ(s)
  }

  def sparkSFA(): Unit = {
    val s = new SparkStrategy()
    val q = getQ(s)
  }

  def getQ(s : RunStrategy) = {
    val img = load[Image]("iaprtc12/images/", InputType.IMAGE)
    val qImg = load[Image](SAMPLE_IMAGES_ROOT + "test/1000.jpg", InputType.IMAGE)

    val colorLayout = img.connect(f_colorLayout)
    val edgeHistogram = img.connect(f_edgeHistogram)
    val gabor = img.connect(f_gabor)
    
    val queryColorLayout = qImg.connect(f_colorLayout)
    val queryEdgeHistogram = qImg.connect(f_edgeHistogram)
    val queryGabor = qImg.connect(f_gabor)
    
    queryColorLayout.accept(s)
    queryColorLayout.cache
    
    colorLayout.connect(f_FeatureDistance(null)).accept(s)
    edgeHistogram.connect(f_FeatureDistance(null)).accept(s)
    gabor.connect(f_FeatureDistance(null)).accept(s)
  }

}