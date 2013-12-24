package edu.uwm.cs.pir.compile

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.pir.misc.Constants._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._

object Function {

  def f_cedd() = new GenericCEDD()

  def f_fcth() = new GenericFCTH()

  def f_sift() = new GenericSIFT()
  
  def f_luceneDocTransformer() = new GenericLuceneDocumentTransformer()
  
  def f_WikiPediaDataSetTextExtractor(extractedTextLocation : String = DATA_ROOT + "text_features/training/") = new GenericWikiPediaDataSetTextExtractor(extractedTextLocation)
  
  def f_ldaTrain = new GenericLDATrain()
  
  def f_kMeansTrain = new GenericKMeansTrain()
  
  def f_cluster = new GenericCluster()
  
  def f_cca = new GenericCCATrain()
  
  def f_transmediaI = new GenericTransmediaQuery[Histogram]()
  
  def f_transmediaT = new GenericTransmediaQuery[LdaFeature]()
  
  def f_ldaProj = new GenericLDA()
  
  def f_weightedQuery() = new GenericLuceneQuery()

  def f_luceneIdx() = {
    val indexer = new LuceneIndexer(INDEX_IMAGE_FEATURE_ROOT)
    new GenericLuceneIndex[Image, IIndex](indexer)
  }

  
}