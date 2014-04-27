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

object NaiveIndexQuery {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      usage
    } else {
      sparkContext = initSparkConf
      awsS3Config = initAWSS3Config
      
      val env = args(0)
      val dataset = args(1)
      
      if ("se" == env) {
        time(sequentialNaiveIndexQuery(dataset)) {
          "sequentialNaiveIndexQuery"
        }
      } else if ("p" == env) {
        time(parallelNaiveIndexQuery(4, dataset)) {
          "parallelNaiveIndexQuery (4 processors)"
        }
      } else if ("sp" == env) {
        time(sparkNaiveIndexQuery(dataset)) {
          "sparkNaiveIndexQuery"
        }
        log("Complete sparkNaiveIndexQuery")("INFO")
      } else {
        usage
      }
      sparkContext.stop
      log("NaiveIndexQuery mission complete")("INFO")
    }
  }

  def usage: Unit = {
    println("USAGE: NaiveIndexQuery \"se/p/sp 1/2 env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("if in Spark execution, 1 to run program against sample, 2 against WikiPedia dataset)")
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialNaiveIndexQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SequentialStrategy()
    q.accept(s)
  }

  def parallelNaiveIndexQuery(numProcessors: Int, dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new ParallelStrategy(numProcessors)
    q.accept(s)
  }

  def sparkNaiveIndexQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SparkStrategy()
    q.accept(s)
  }

  def getQ(dataset: String) = {
    val img = load[Image]((if ("1" == dataset) SAMPLE_IMAGES_ROOT else WIKIPEDIA_IMAGES_ROOT) + "training", InputType.IMAGE)
    val qImg = load[Image](SAMPLE_IMAGES_ROOT + "test/05fd84a06ea4f6769436760d8c5986c8.jpg", InputType.IMAGE)

    val siftImg = img.connect(f_sift)
    val kModel = train(f_kMeansTrain, siftImg)
    val idx = naiveIndex(f_histogramIdx, siftImg.connect(f_cluster, kModel).connect(f_histogramString))

    invertedIndexQuery(f_invertedIndexQuery, idx, qImg.connect(f_sift).connect(f_cluster, kModel).connect(f_histogramString))
  }

}