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

object ImageQuery {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      usage
    } else {
      
      sparkContext = initSparkConf
      awsS3Config = initAWSS3Config
      
      val env = args(0)
      val dataset = args(1)
      
      if ("se" == env) {
        time(sequentialImageQuery(dataset)) {
          "sequentialImageQuery"
        }
      } else if ("p" == env) {
        time(parallelImageQuery(4, dataset)) {
          "parallelImageQuery (4 processors)"
        }
      } else if ("sp" == env) {
        time(sparkImageQuery(dataset)) {
          "sparkImageQuery"
        }
        log("Complete sparkImageQuery")("INFO")
        //    time(sparkTransmediaQuery()) {
        //      "jobVisit"
        //    }
      } else {
        usage
      }
      sparkContext.stop
      log("ImageQuery mission complete")("INFO")
    }
  }

  def usage: Unit = {
    println("USAGE: ImageQuery \"se/p/sp 1/2 env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("if in Spark execution, 1 to run program against sample, 2 against WikiPedia dataset)")
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialImageQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SequentialStrategy()
    q.accept(s)
  }

  def parallelImageQuery(numProcessors: Int, dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new ParallelStrategy(numProcessors)
    q.accept(s)
  }

  def sparkImageQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SparkStrategy()
    q.accept(s)
  }

  def getQ(dataset: String) = {
    val img = load[Image]((if ("1" == dataset) SAMPLE_IMAGES_ROOT else WIKIPEDIA_IMAGES_ROOT) + "training", InputType.IMAGE)
    val qImg = load[Image](SAMPLE_IMAGES_ROOT + "test/05fd84a06ea4f6769436760d8c5986c8.jpg", InputType.IMAGE)

    val idx = index(f_luceneIdx, img.connect(f_cedd).connect(f_luceneDocTransformer), img.connect(f_fcth).connect(f_luceneDocTransformer))

    query(f_weightedQuery, idx, qImg)
  }

}