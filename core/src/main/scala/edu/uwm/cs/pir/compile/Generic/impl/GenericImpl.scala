package edu.uwm.cs.pir.compile.Generic.impl

import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.spark.SparkObject._
import edu.uwm.cs.pir.aws.AWSS3API._
import edu.uwm.cs.pir.compile.Function._
import edu.uwm.cs.mir.prototypes.feature.lire._
import edu.uwm.cs.mir.prototypes.feature.lucene._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.proj._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.proj.lire._
import edu.uwm.cs.mir.prototypes.train._
import edu.uwm.cs.mir.prototypes.proj.wikipedia._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.feature.wikipedia._
import edu.uwm.cs.mir.prototypes.feature.FilenameFeature._
import edu.uwm.cs.mir.prototypes.feature.AWSS3Source
import edu.uwm.cs.pir.misc.Constants._
import org.apache.commons.io.FileUtils
import net.semanticmetadata.lire.imageanalysis.LireFeature

object GenericImpl {

  import edu.uwm.cs.mir.prototypes.feature._
  import edu.uwm.cs.mir.prototypes.index._
  import java.io.File
  import edu.uwm.cs.pir.compile.Generic.GenericInterface._
  import edu.uwm.cs.mir.prototypes.feature.utils._
  import scala.collection.JavaConverters._

  @SerialVersionUID(1L)
  case class GenericTextLoad[Out <: IFeature](val url: String) extends GenericLoad[Out] {
    override lazy val fileList: List[String] = {
      if (url.endsWith(".xml")) {
        //This is for the case of query or such
        List(url)
      } else {
        //log("text url = " + url)
        if (awsS3Config.isIs_s3_storage()) getIdList(url, "xml") else FeatureUtils.getFilenameListByPath(url, "xml").asScala.toList
      }
    }

    def apply(url: String): Option[Out] = {
      if (url.isEmpty()) {
        None
      } else {
        val text = new Text(url).asInstanceOf[Out]
        if (awsS3Config.isIs_s3_storage()) {
          //log("load text from AWS")
          text.setAWSS3Config(awsS3Config)
        }
        log("load text : " + url)("INFO")
        Some(text)
      }
    }
  }

  @SerialVersionUID(1L)
  case class GenericImageLoad[Out <: IFeature](val url: String) extends GenericLoad[Out] {
    override lazy val fileList: List[String] = {
      if (url.endsWith(".jpg")) {
        //This is for the case of query or such
        List(url)
      } else {
        //log("image url = " + url)
        if (awsS3Config.isIs_s3_storage()) {
          //log("isIs_s3_storage = true")
          getIdList(url, "jpg")
        } else {
          //log("isIs_s3_storage = false")
          FeatureUtils.getFilenameListByPath(url, "jpg").asScala.toList
        }
      }
    }

    def apply(url: String): Option[Out] = {
      if (url.isEmpty()) {
        None
      } else {
        val image = new Image(url).asInstanceOf[Out]
        if (awsS3Config.isIs_s3_storage()) {
          //log("load image from AWS bucket name = " + awsS3Config.getBucket_name())("INFO")
          image.setAWSS3Config(awsS3Config)
        }
        //log("load image : " + url)("INFO")
        Some(image)
      }
    }
  }

  @SerialVersionUID(1L)
  case class GenericLuceneIndex[In <: IFeature, Index <: IIndex](val indexer: IIndexer) extends GenericIndex[In, Index] {
    def apply(in: List[List[In]]): Index = {
      log("Apply Lucene Index to " + in.getClass().getCanonicalName())("INFO")
      indexer.apply((in.map(innerList => innerList.asJava)).asJava).asInstanceOf[Index]
    }

    def index(featureList: Seq[SourceComponent[LuceneFeatureAdaptor]]): IndexStage[In, Index] = {
      new IndexStage[In, Index](this, featureList.toList.map(elem => elem.asInstanceOf[SourceComponent[In]]))
    }

  }

  @SerialVersionUID(1L)
  case class GenericLuceneQuery() extends GenericProjWithIndex[IFeature, LuceneQueryResultAdaptor, LuceneIndex] {
    def apply(in: IFeature): LuceneQueryResultAdaptor = {
      val luceneQuery = new LuceneQuery()
      luceneQuery.setIndex(this.index.get)
      luceneQuery.apply(in).asInstanceOf[LuceneQueryResultAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {
      this.index = Some(index.asInstanceOf[LuceneIndex])
    }

    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericLuceneCompose[In <: IFeature, Compose <: ICompose](weights: List[Double]) extends GenericCompose[In, Compose] {
    val composer = new LuceneWeightedComposer(weights.map(weight => weight.asInstanceOf[java.lang.Double]).asJava)
    def apply(in: List[List[In]]): Compose = {
      val list = in.map(innerList => innerList.asJava)
      composer.apply(list.asJava).asInstanceOf[Compose]
    }
  }

  @SerialVersionUID(1L)
  case class GenericFeatureDistance(val queryFeature: LireFeatureAdaptor) extends GenericProj[LireFeatureAdaptor, LireDistanceFeatureAdaptor] {
     
    override def apply(in: LireFeatureAdaptor): LireDistanceFeatureAdaptor = {
      log("Apply FeatureDistance to " + in.getId())("INFO")
      //log("Source LireFeature ByteArrayRepresentation is " + in.getLireFeature().getByteArrayRepresentation().map(elem => elem + ". "))("INFO")
      //log("Target LireFeature ByteArrayRepresentation is " + cachedQueryFeature.getLireFeature().getByteArrayRepresentation().map(elem => elem + ". "))("INFO")
      val distance : Float = try {
        in.getLireFeature().getDistance(queryFeature.getFeature())
      } catch {
        case npe : NullPointerException => -1F
        case e : Exception => throw new RuntimeException(e)
      }
      new LireDistanceFeatureAdaptor(in.getId(), distance)
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericColorLayout(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT) extends GenericProj[Image, LireFeatureAdaptor] {
    val colorLayout = new ColorLayout(scaleWidth, scaleHeight)
    override def apply(in: Image): LireFeatureAdaptor = {
      log("Apply ColorLayout to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) colorLayout.setAWSS3Config(awsS3Config)
      colorLayout.apply(in).asInstanceOf[LireFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericEdgeHistogram(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT) extends GenericProj[Image, LireFeatureAdaptor] {
    val edgeHistogram = new EdgeHistogram(scaleWidth, scaleHeight)
    override def apply(in: Image): LireFeatureAdaptor = {
      log("Apply EdgeHistogram to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) edgeHistogram.setAWSS3Config(awsS3Config)
      edgeHistogram.apply(in).asInstanceOf[LireFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericGabor(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT) extends GenericProj[Image, LireFeatureAdaptor] {
    val gabor = new Gabor(scaleWidth, scaleHeight)
    override def apply(in: Image): LireFeatureAdaptor = {
      log("Apply Gabor to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) gabor.setAWSS3Config(awsS3Config)
      gabor.apply(in).asInstanceOf[LireFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericCEDD(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT) extends GenericProj[Image, LireFeatureAdaptor] {
    val cedd = new CEDD(scaleWidth, scaleHeight)
    override def apply(in: Image): LireFeatureAdaptor = {
      log("Apply CEDD to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) cedd.setAWSS3Config(awsS3Config)
      cedd.apply(in).asInstanceOf[LireFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericFCTH(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT) extends GenericProj[Image, LireFeatureAdaptor] {
    val fcth = new FCTH(scaleWidth, scaleHeight)
    override def apply(in: Image): LireFeatureAdaptor = {
      log("Apply FCTH to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) fcth.setAWSS3Config(awsS3Config)
      fcth.apply(in).asInstanceOf[LireFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericSIFT(scaleWidth: Int = SCALE_WIDTH, scaleHeight: Int = SCALE_HEIGHT, numberOfFeatures: Int = NUM_OF_FEATURES)
    extends GenericProj[Image, SiftFeatureAdaptor] {
    val sift = new SIFT(scaleWidth, scaleHeight, numberOfFeatures)
    override def apply(in: Image): SiftFeatureAdaptor = {
      log("Apply SIFT to " + in.getId() /* + " with " + in.getFeature() */ )("INFO")
      if (awsS3Config.isIs_s3_storage()) sift.setAWSS3Config(awsS3Config)
      sift.apply(in).asInstanceOf[SiftFeatureAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericWikiPediaDataSetTextExtractor(extractedTextLocation: String) extends GenericProj[Text, WikiPediaTextAdaptor] {
    val wikiPediaDataSetTextExtract = new WikiPediaDataSetTextExtract(extractedTextLocation)
    override def apply(in: Text): WikiPediaTextAdaptor = {
      log("Apply WikiPediaDataSetTextExtract to " + in)("INFO")
      if (awsS3Config.isIs_s3_storage()) wikiPediaDataSetTextExtract.setAWSS3Config(awsS3Config)
      wikiPediaDataSetTextExtract.apply(in).asInstanceOf[WikiPediaTextAdaptor]
    }

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}
  }

  @SerialVersionUID(1L)
  case class GenericLDATrain(
    ldaModelFileLocation: String = LDA_MODEL_FIlE,
    descFile: String = EXTRACTED_TEXT_FILE_ROOT + "ALL_DESC.txt",
    stopwordFile: String = STOPWORDS_ROOT + "en.txt",
    numberOfTopics: Int = NUM_OF_TOPICS,
    alphaSum: Double = ALPHA_SUM,
    betaW: Double = BETA_W,
    numberOfSamplers: Int = NUMBER_SAMPLER,
    numberOfIterations: Int = NUMBER_ITERATION,
    encoding: String = DEFAULT_ENCODING) extends GenericTrain[WikiPediaTextAdaptor, LdaModel] {

    val ldaTrain = new LDATrain(ldaModelFileLocation, descFile, stopwordFile, numberOfTopics,
      alphaSum, betaW, numberOfSamplers, numberOfIterations, encoding)
    override def apply(in: List[List[WikiPediaTextAdaptor]]): LdaModel = {
      log("Apply LDA Train to " + in.getClass().getCanonicalName())("INFO")
      if (awsS3Config.isIs_s3_storage()) {
        ldaTrain.setAWSS3Config(awsS3Config)
      }
      ldaTrain.apply((in.map(list => list.asJava)).asJava).asInstanceOf[LdaModel]
    }
  }

  @SerialVersionUID(1L)
  case class GenericKMeansTrain(clusterFilename: String = CLUSTER_FIlE, numberOfClusters: Int = NUM_OF_CLUSTERS)
    extends GenericTrain[SiftFeatureAdaptor, ClusterModel] {
    val kMeansTrain = new KMeansTrain(clusterFilename, numberOfClusters)
    override def apply(in: List[List[SiftFeatureAdaptor]]): ClusterModel = {
      log("Apply kMeansTrain Train to " + in.getClass().getCanonicalName())("INFO")
      if (awsS3Config.isIs_s3_storage()) {
        kMeansTrain.setAWSS3Config(awsS3Config)
      }
      kMeansTrain.apply((in.map(list => list.asJava)).asJava).asInstanceOf[ClusterModel]
    }
  }

  @SerialVersionUID(1L)
  case class GenericCluster(numberOfClusters: Int = NUM_OF_CLUSTERS)
    extends GenericProjWithModel[SiftFeatureAdaptor, Histogram, ClusterModel] {

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {
      this.model = Some(model.asInstanceOf[ClusterModel])
    }

    val cluster = new ClusterProj(numberOfClusters)

    override def apply(in: SiftFeatureAdaptor): Histogram = {
      log("Apply Cluster to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) cluster.setAWSS3Config(awsS3Config)
      cluster.setModel(this.model.get)
      cluster.apply(in).asInstanceOf[Histogram]
    }
  }

  @SerialVersionUID(1L)
  case class GenericLDA(stopwordFile: String = STOPWORDS_ROOT + "en.txt", gibbsSamplingIteration: Int = GIBBS_SAMPLING_ITERATION,
    gibbsSamplingThinning: Int = GIBBS_SAMPLING_THINNING, gibbsSamplingBurnin: Int = GIBBS_SAMPLING_BURNIN, encoding: String = DEFAULT_ENCODING)
    extends GenericProjWithModel[WikiPediaTextAdaptor, LdaFeature, LdaModel] {
    val lda = new LDA(stopwordFile, gibbsSamplingIteration, gibbsSamplingThinning, gibbsSamplingBurnin, encoding)

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {
      this.model = Some(model.asInstanceOf[LdaModel])
    }

    def apply(in: WikiPediaTextAdaptor): LdaFeature = {
      lda.setModel(this.model.get)
      //val text = in.getFeature
      log("Apply LDA to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) lda.setAWSS3Config(awsS3Config)
      lda.apply(in).asInstanceOf[LdaFeature]
    }
  }

  @SerialVersionUID(1L)
  case class GenericLuceneDocumentTransformer()
    extends GenericProj[LireFeatureAdaptor, LuceneFeatureAdaptor] {
    val ldt = new LuceneDocumentTransformer()

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {}

    def apply(in: LireFeatureAdaptor): LuceneFeatureAdaptor = {
      log("Apply LuceneDocumentTransformer to " + in.getId())("INFO")
      if (awsS3Config.isIs_s3_storage()) {
        ldt.setAWSS3Config(awsS3Config)
      }
      //log("in.id=" + in.getId() + ", in.lireFeature=" + in.getLireFeature() + ", in.type=" + in.getType())("DEBUG")
      ldt.apply(in).asInstanceOf[LuceneFeatureAdaptor]
    }
  }

  @SerialVersionUID(1L)
  case class GenericCCATrain(imageFeatureSize: Int = NUM_OF_CLUSTERS, textFeatureSize: Int = if (awsS3Config.isIs_s3_storage()) {
    edu.uwm.cs.mir.prototypes.aws.AWSS3API.getNumberOfLinesOfS3Objects(awsS3Config, GROUND_TRUTH_CATEGORY_LIST, edu.uwm.cs.mir.prototypes.aws.AWSS3API.getAmazonS3Client(awsS3Config))
  } else {
    FileUtils.readLines(new File(GROUND_TRUTH_CATEGORY_LIST)).size()
  }) extends GenericTrain2[Histogram, LdaFeature, CCAModel] {

    val ccaTrain = new CCATrain(imageFeatureSize, textFeatureSize)
    override def apply(in: List[List[IFeature]]): CCAModel = {
      log("Apply CCATrain Train to " + in.getClass().getCanonicalName())("INFO")
      if (awsS3Config.isIs_s3_storage()) {
        ccaTrain.setAWSS3Config(awsS3Config)
      }
      ccaTrain.apply((in.map(list => list.asJava)).asJava).asInstanceOf[CCAModel]
    }
  }

  @SerialVersionUID(1L)
  case class GenericTransmediaQuery[In <: IFeature](numberOfTopResults: Int = 100, groungTruthcategoryFilename: String = GROUND_TRUTH_CATEGORY_LIST)
    extends GenericProjWithModel[In, QueryResultFeature, CCAModel] {
    val transmediaQuery = new TransmediaQuery(numberOfTopResults, groungTruthcategoryFilename)

    override def setIndex(index: IIndex): Unit = {}
    override def setModel(model: IModel): Unit = {
      this.model = Some(model.asInstanceOf[CCAModel])
    }

    def apply(in: In): QueryResultFeature = {
      log("Apply TransmediaQuery to " + in)("INFO")
      transmediaQuery.setModel(this.model.get)
      if (awsS3Config.isIs_s3_storage()) {
        transmediaQuery.setAWSS3Config(awsS3Config)
      }
      val queryResultFeature = transmediaQuery.apply(in).asInstanceOf[QueryResultFeature]
      log(queryResultFeature.getFeature)
      queryResultFeature
    }
  }
}