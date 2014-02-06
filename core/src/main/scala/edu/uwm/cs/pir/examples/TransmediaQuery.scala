package edu.uwm.cs.pir.examples

import common._
import com.typesafe.config._

import edu.uwm.cs.mir.prototypes.feature._

import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.misc.InputType
import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.misc.Constants._
import edu.uwm.cs.pir.compile.Compile._
import edu.uwm.cs.pir.compile.Function._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.compile._
import edu.uwm.cs.pir.spark.SparkObject._

object TransmediaQuery {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      usage
    } else {
      
      sparkContext = initSparkConf
      awsS3Config = initAWSS3Config
      
      val env = args(0)
      val dataset = args(1)
      
      if ("se" == env) {
        time(sequentialTransmediaQuery(dataset)) {
          "sequentialTransmediaQuery"
        }
      } else if ("p" == env) {
        time(parallelTransmediaQuery(4, dataset)) {
          "parallelTransmediaQuery (4 processors)"
        }
      } else if ("sp" == env) {
        time(sparkTransmediaQuery(dataset)) {
          "sparkTransmediaQuery"
        }
        //    time(sparkTransmediaQuery()) {
        //      "jobVisit"
        //    }
      } else {
        usage
      }
      sparkContext.stop
      log("TransmediaQuery mission complete")("INFO")
    }
  }

  def usage: Unit = {
    println("USAGE: ImageQuery \"se/p/sp 1/2 env.conf\"");
    println("where se to run program sequentially, " + "p parallely, and sp in Spark; ");
    println("if in Spark execution, 1 to run program against sample, 2 against WikiPedia dataset)")
    println("env.conf is a customized configuration file to replace the default one")
    println("see sample-application.conf for details")
  }

  def sequentialTransmediaQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SequentialStrategy()
    q.accept(s)
  }

  def parallelTransmediaQuery(numProcessors: Int, dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new ParallelStrategy(numProcessors)
    q.accept(s)
  }

  def sparkTransmediaQuery(dataset: String): Unit = {
    val q = getQ(dataset)
    val s = new SparkStrategy()
    q.accept(s)
  }

  def jobVisit(): Unit = {
    val q = getQ("1")
    val s = new JobVisitor()
    q.accept(s)
  }

  def getQ(dataset: String) = {

    val txt = load[Text]((if ("1" == dataset) SAMPLE_TEXT_ROOT else WIKIPEDIA_TEXT_ROOT) + "training/", InputType.TEXT)
    val img = load[Image]((if ("1" == dataset) SAMPLE_IMAGES_ROOT else WIKIPEDIA_IMAGES_ROOT) + "training/", InputType.IMAGE)
    val qImg = load[Image](SAMPLE_IMAGES_ROOT + "test/05fd84a06ea4f6769436760d8c5986c8.jpg", InputType.IMAGE)

    val siftImg = img.connect(f_sift)

    val wikiText = txt.connect((if ("1" == dataset) f_WikiPediaDataSetTextExtractor() else f_WikiPediaDataSetTextExtractor(DATA_ROOT + "text_features/training/")))
    
    val lModel = train(f_ldaTrain, wikiText)
    val kModel = train(f_kMeansTrain, siftImg)

    val projTxt = wikiText.connect(f_ldaProj, lModel)
    val projImg = siftImg.connect(f_cluster, kModel)

    val ccaModel = train(f_cca, projTxt, projImg)
    //    qImg.connect(f_sift)
    //      .connect(f_cluster, kModel)
    //      .connect(f_transmediaI, ccaModel)

    val f = f_sift.connect(f_cluster, kModel).connect(f_transmediaI, ccaModel)
    qImg.connect(f)

  }

}