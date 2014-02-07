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
    //    val img = load[Image]("iaprtc12/images/", InputType.IMAGE)
    //
    //    val colorLayout = img.connect(f_colorLayout)
    //    val edgeHistogram = img.connect(f_edgeHistogram)
    //    val gabor = img.connect(f_gabor)
    //
    //    colorLayout.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout)).accept(s)
    //    edgeHistogram.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_edgeHistogram)).accept(s)
    //    gabor.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor)).accept(s)

    //    val img1 = load[Image]("images", InputType.IMAGE)
    //    val colorLayout = img1.connect(f_colorLayout)
    //    val colorLayoutDis = colorLayout.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout)).sort("ascending")
    //    val img2 = img1.filter(f_top(colorLayoutDis.asInstanceOf[SourceComponent[IFeature]], 2000))
    //    val cedd = img2.connect(f_cedd)
    //    val ceddDis = cedd.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_cedd)).sort("ascending")
    //    val img3 = img2.filter(f_top(ceddDis.asInstanceOf[SourceComponent[IFeature]], 500))
    //    val gabor = img3.connect(f_gabor)
    //    val gaborDis = gabor.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor)).sort("ascending")
    //    val img4 = img3.filter(f_top(gaborDis.asInstanceOf[SourceComponent[IFeature]], 100))
    //    img4.accept(s)

    val img1 = load[Image](SAMPLE_IMAGES_ROOT + "training", InputType.IMAGE)
    val colorLayout = img1.connect(f_colorLayout)
    val colorLayoutDis = colorLayout.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_colorLayout)).sort("ascending")
    val img2 = img1.filter(f_top(colorLayoutDis.collect.take(2000)))
    val cedd = img2.connect(f_cedd)
    val ceddDis = cedd.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_cedd)).sort("ascending")
    val img3 = img2.filter(f_top(ceddDis.collect.take(500)))
    val gabor = img3.connect(f_gabor)
    val gaborDis = gabor.connect(f_FeatureDistance(SAMPLE_IMAGES_ROOT + "test/1000.jpg", f_gabor)).sort("ascending")
    val img4 = img3.filter(f_top(gaborDis.collect.take(100)))
    img4.accept(GLOBAL_STRATEGY)
  }

}