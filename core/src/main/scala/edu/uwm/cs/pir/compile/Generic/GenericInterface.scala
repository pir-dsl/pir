package edu.uwm.cs.pir.compile.Generic

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.feature.lire._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.composer._

import edu.uwm.cs.pir.graph._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Source._

object GenericInterface {

  trait GenericLoad[Out <: IFeature] extends Serializable {
    val fileList: List[String]
    //def apply(list: List[String]): Option[List[Out]]
    def apply(url: String): Option[Out]
  }

  trait BasicIndex extends Serializable {
	  
  }
  
  trait BasicIndexer[In <: IFeature] {
	def apply(qs : List[In]) : BasicIndex
	def getName() : String
  }
  
  trait GenericIndex[In <: IFeature, Index <: IIndex] extends Serializable {
    def apply(in: List[List[In]]): Index
    def index(source: List[SourceComponent[In]]): IndexStage[In, Index] = {
      new IndexStage[In, Index](this, source)
    }
  }
  
  trait GenericBasicIndex[In <: IFeature, Index <: BasicIndex] extends Serializable {
    def apply(in: List[In]): Index
    def index(source: SourceComponent[In]): HistogramIndexStage[In, Index] = {
      new HistogramIndexStage[In, Index](this, source)
    }
  }

  trait GenericProj[In <: IFeature, Out <: IFeature] extends Serializable {
    def apply(in: In): Out
    def setIndex(index: IIndex): Unit
    def setModel(model: IModel): Unit
  }

  trait GenericProjWithIndex[In <: IFeature, Out <: IFeature, Index <: IIndex] extends GenericProj[In, Out] with Serializable {
    var index: Option[Index] = None
    def apply(in: In): Out
  }

  trait GenericCompose[In <: IFeature, Compose <: ICompose] extends Serializable {
    def apply(in: List[List[In]]): Compose
  }

  trait GenericTrain[In <: IFeature, Model <: IModel] extends Serializable {
    def apply(in: List[List[In]]): Model
    def train(source: SourceComponent[In]): TrainStage[In, Model] = {
      new TrainStage[In, Model](this, source)
    }
  }

  trait GenericTrain2[In1 <: IFeature, In2 <: IFeature, Model <: IModel] extends Serializable {
    def apply(in: List[List[IFeature]]): Model
    def train(source1: SourceComponent[In1], source2: SourceComponent[In2]): TrainStage2[In1, In2, Model] = {
      new TrainStage2[In1, In2, Model](this, source1, source2)
    }
  }

  trait GenericProjWithModel[In <: IFeature, Out <: IFeature, Model <: IModel] extends GenericProj[In, Out] with Serializable {
    var model: Option[Model] = None
    def apply(in: In): Out
  }
}