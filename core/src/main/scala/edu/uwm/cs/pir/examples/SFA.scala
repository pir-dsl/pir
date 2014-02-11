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
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.spark.SparkObject._

object SFA {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      usage
    } else {

      sparkContext = initSparkConf
      awsS3Config = initAWSS3Config
      GLOBAL_STRATEGY = new SparkStrategy()

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
    println("USAGE: SFA \"se/p/sp env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialSFA(): Unit = {
    GLOBAL_STRATEGY = new SequentialStrategy()
    val q = getQ
  }

  def parallelSFA(numProcessors: Int): Unit = {
    GLOBAL_STRATEGY = new ParallelStrategy(numProcessors)
    val q = getQ
  }

  def sparkSFA(): Unit = {
    val q = getQ
  }

  def getQ() = {

    val colorFeatureAdaptor = f_lireFeatureAdaptor(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout)
    val ceddFeatureAdaptor = f_lireFeatureAdaptor(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_cedd)
    val gaborFeatureAdaptor = f_lireFeatureAdaptor(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor)
    
    val pipe1 = f_colorLayout.connect(f_featureDistance(colorFeatureAdaptor))
    val pipe2 = f_cedd.connect(f_featureDistance(ceddFeatureAdaptor))
    val pipe3 = f_gabor.connect(f_featureDistance(gaborFeatureAdaptor))
    
    val img1 = load[Image]("images", InputType.IMAGE) 
    val colorLayoutDist = img1.connect(pipe1).sort("ascending").collect.take(2000)
    val img2 = img1.filter(f_top(colorLayoutDist))
    val ceddDist = img2.connect(pipe2).sort("ascending").collect.take(500)
    val img3 = img2.filter(f_top(ceddDist))
    val gaborDist = img3.connect(pipe3).sort("ascending").collect.take(100)
    val img4 = img3.filter(f_top(gaborDist))
    img4.accept(GLOBAL_STRATEGY)
    img4.printIds
  }

}
