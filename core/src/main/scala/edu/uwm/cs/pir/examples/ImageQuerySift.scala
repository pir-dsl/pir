package edu.uwm.cs.pir.examples

import common._
import com.typesafe.config._

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.feature.lire._
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

object ImageQuerySift {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      usage
    } else {
      
      sparkContext = initSparkConf
      awsS3Config = initAWSS3Config
      
      val env = args(0)
      
      if ("se" == env) {
        time(sequentialImageQuery) {
          "sequentialImageQuery"
        }
      } else if ("p" == env) {
        time(parallelImageQuery(4)) {
          "parallelImageQuery (4 processors)"
        }
      } else if ("sp" == env) {
        time(sparkImageQuery) {
          "sparkImageQuery"
        }
        log("Complete sparkImageQuery")("INFO")
        //    time(sparkTransmediaQuery) {
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
    println("USAGE: ImageQuery \"se/p/sp env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialImageQuery(): Unit = {
    val q = getQ
    val s = new SequentialStrategy()
    q.accept(s)
  }

  def parallelImageQuery(numProcessors: Int): Unit = {
    val q = getQ
    val s = new ParallelStrategy(numProcessors)
    q.accept(s)
  }

  def sparkImageQuery(): Unit = {
    val q = getQ
    val s = new SparkStrategy()
    q.accept(s)
  }

  def getQ() = {
    val feature = load[SiftFeatureAdaptor]("", InputType.FEATURE)
    val qImg = load[Image](SAMPLE_SIFT_FEATURE_ROOT + "1423-0127-17-3-4.sift", InputType.FEATURE)

    val idx = index(f_luceneIdx, feature.connect(f_luceneDocTransformerSift), feature.connect(f_luceneDocTransformerSift))

    query(f_weightedQuery, idx, qImg)
  }

}