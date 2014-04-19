package edu.uwm.cs.pir.compile

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.pir.misc.Constants._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.spark.SparkObject._
import edu.uwm.cs.mir.prototypes.feature.lire._
import net.semanticmetadata.lire.imageanalysis.LireFeature

//import org.apache.spark.rdd._

object Function {

  def f_lireFeatureAdaptor(queryImagePath: String, proj: GenericProj[Image, LireFeatureAdaptor]) : LireFeatureAdaptor = {
    proj.apply({
      val image: Image = new Image(queryImagePath)
      if (awsS3Config.isIs_s3_storage()) image.setAWSS3Config(awsS3Config)
      image
    })
  }
  
  def f_featureDistance(lireFeatureAdaptor: LireFeatureAdaptor) = {
    new GenericFeatureDistance(lireFeatureAdaptor)
  }

  def f_top[In <: IFeature](listP: List[LireDistanceFeatureAdaptor]): In => Boolean = {
    val list = listP
    in =>
      {
        list.map(elem => elem.getId).contains(in.getId)
      }
  }

  //def f_dummyColorLayout() = new GenericDummyColorLayout()
  
  def f_colorLayout() = new GenericColorLayout()

  def f_edgeHistogram() = new GenericEdgeHistogram()

  def f_gabor() = new GenericGabor()

  def f_cedd() = new GenericCEDD()

  def f_fcth() = new GenericFCTH()

  def f_sift() = new GenericSIFT()

  def f_luceneDocTransformer() = new GenericLuceneDocumentTransformer()

  def f_WikiPediaDataSetTextExtractor(extractedTextLocation: String = DATA_ROOT + "text_features/training/") = new GenericWikiPediaDataSetTextExtractor(extractedTextLocation)

  def f_ldaTrain = new GenericLDATrain()

  def f_kMeansTrain = new GenericKMeansTrain()

  def f_cluster = new GenericCluster()

  def f_cca = new GenericCCATrain()

  def f_transmediaI = new GenericTransmediaQuery[Histogram]()

  def f_transmediaT = new GenericTransmediaQuery[LdaFeature]()

  def f_ldaProj = new GenericLDA()

  def f_weightedQuery() = new GenericLuceneQuery()
  
  def f_naiveIndexQuery() = new GenericNaiveIndexQuery()

  def f_luceneIdx() = {
    val indexer = new LuceneIndexer(INDEX_IMAGE_FEATURE_ROOT)
    new GenericLuceneIndex[Image, IIndex](indexer)
  }
  
  def f_histogramIdx() = {
    val indexer = new GenericInvertedIndexer()
    new GenericHistogramIndex[HistogramString, IIndex](indexer)
  }
  
  def f_histogramString() = {
    new GenericHistogramString()
  }

}
