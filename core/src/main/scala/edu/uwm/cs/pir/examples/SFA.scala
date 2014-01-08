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
      awsS3Config = initAWSS3Config

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

  def getQ(s: RunStrategy) = {
    //    val img = load[Image]("iaprtc12/images/", InputType.IMAGE)
    //
    //    val colorLayout = img.connect(f_colorLayout)
    //    val edgeHistogram = img.connect(f_edgeHistogram)
    //    val gabor = img.connect(f_gabor)
    //    
    //    colorLayout.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout)).accept(s)
    //    edgeHistogram.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_edgeHistogram)).accept(s)
    //    gabor.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor)).accept(s)

    val img = load[Image]("images", InputType.IMAGE)

    val colorLayout = img.connect(f_colorLayout)
    val edgeHistogram = img.connect(f_edgeHistogram)
    val gabor = img.connect(f_gabor)

    val res1 = colorLayout.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout))
    res1.accept(s)
    res1.cache = None
    val res2 = edgeHistogram.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_edgeHistogram))
    res2.accept(s)
    res2.cache = None
    val res3 = gabor.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor))
    res3.accept(s)
    res3.cache = None
    //val qImg = load[Image](SAMPLE_IMAGES_ROOT + "test/05fd84a06ea4f6769436760d8c5986c8.jpg", InputType.IMAGE)

    //val idx = index(f_luceneIdx, img.connect(f_cedd).connect(f_luceneDocTransformer), img.connect(f_fcth).connect(f_luceneDocTransformer))

    //query(f_weightedQuery, idx, qImg).accept(s)
  }

}