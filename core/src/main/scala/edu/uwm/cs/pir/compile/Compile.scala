package edu.uwm.cs.pir.compile

import edu.uwm.cs.pir.misc.InputType._
import edu.uwm.cs.pir.misc.InputType
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.compile.Function._

import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.feature.lire._
import edu.uwm.cs.mir.prototypes.feature.lucene._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.feature.wikipedia._

import scala.reflect.ClassTag

object Compile {

  import edu.uwm.cs.pir.compile.Generic.GenericInterface._
  import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
  def load[Out <: IFeature : ClassTag](path: String, inputType: InputType): SourceComponent[Out] = {
    if (inputType == InputType.IMAGE) {
      new GenericImageLoad[Out](path)
    } else if (inputType == InputType.TEXT) {
      new GenericTextLoad[Out](path)
    } else {
      throw new Exception("The type " + inputType + " is not supported yet!")
    }
  }

  def index(idx: GenericLuceneIndex[Image, IIndex], featureList: SourceComponent[LuceneFeatureAdaptor]*): IndexStage[Image, IIndex] = {
    idx.index(featureList)
  }
  
  def naiveIndex(idx: GenericHistogramIndex[HistogramString, IIndex], featureSource: SourceComponent[HistogramString]): HistogramIndexStage[HistogramString, IIndex] = {
    idx.index(featureSource)
  }

  def invertedIndexQuery(query: GenericNaiveIndexQuery, idx: HistogramIndexStage[HistogramString, IIndex], qFeature: SourceComponent[HistogramString]) = {
    qFeature.connect(query, idx)
  }
  
  def query(query: GenericLuceneQuery, idx: IndexStage[Image, IIndex], qImg: SourceComponent[Image], ratio: Double = 0.5) = {
    val weights: List[Double] = List(ratio, 1 - ratio)
    val compose = new GenericLuceneCompose[Image, LuceneWeightedQueryResult](weights)
    qImg.connect(query, idx, compose)
  }
  
  def train(trainer: GenericLDATrain, wikiText: SourceComponent[WikiPediaTextAdaptor]): TrainStage[WikiPediaTextAdaptor, LdaModel] = {
    new TrainStage[WikiPediaTextAdaptor, LdaModel](trainer, wikiText)
  }

  def train(trainer: GenericKMeansTrain, imgStage: SourceComponent[SiftFeatureAdaptor]): TrainStage[SiftFeatureAdaptor, ClusterModel] = {
    new TrainStage[SiftFeatureAdaptor, ClusterModel](trainer, imgStage)
  }

  def train(trainer: GenericCCATrain, txtSource: SourceComponent[LdaFeature], imgSource: SourceComponent[Histogram]): TrainStage2[Histogram, LdaFeature, CCAModel] = {
    new TrainStage2[Histogram, LdaFeature, CCAModel](trainer, imgSource, txtSource)
  }
}