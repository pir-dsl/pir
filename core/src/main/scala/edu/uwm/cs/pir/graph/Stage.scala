package edu.uwm.cs.pir.graph

import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.compile.Compile._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.feature.lire._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import edu.uwm.cs.pir.compile.Visitor

object Stage {

  @SerialVersionUID(1L)
  class LoadStage[In <: IFeature, Out <: IFeature : ClassTag](val load: GenericLoad[Out])
    extends SourceComponent[Out] with Serializable {

    override def accept(v: Visitor) = v.visit(this)
    override def toString() = "Load Stage-" + load
  }

  @SerialVersionUID(1L)
  class ProjStage[In <: IFeature, Out <: IFeature](val proj: GenericProj[In, Out])
    extends ProjComponent[In, Out] with Serializable {

    override def accept(v: Visitor) = v.visit(this)
    override def toString() = "Proj Stage-" + proj
   
    def apply(f : In) = proj.apply(f)
  }

  @SerialVersionUID(1L)
  class ProjWithModelStage[In <: IFeature, Out <: IFeature, Model <: IModel]
                                                 (override val proj: GenericProjWithModel[In, Out, Model],
                                                           val train: TrainComponent[Model])
    extends ProjStage[In, Out](proj) with Serializable {

    override def accept(v: Visitor) = v.visit(this)
    override def toString() = "Proj With Model Stage-" + proj
    
    override def apply(f : In) = {/*log("This should be run, proj = " + proj + ", f = " + f);*/ proj.apply(f)}
  }

  trait TrainComponent[Model <: IModel] extends Vertex {
     def getModel() : Option[Model] 
     
    var isDirty = false
    var cacheModel: Option[Model] = None
  }
  
  @SerialVersionUID(1L)
  class TrainStage[In <: IFeature, Model <: IModel](val trainer: GenericTrain[In, Model],
    var source: SourceComponent[In])
    extends TrainComponent[Model] with Serializable {

    def getModel() : Option[Model] = cacheModel
    def setModel(model : Option[Model]) : Unit = {
      cacheModel = model
    }
    
    override def accept(v: Visitor) = v.visit(this)
    
    override def toString = "Train Stage-" + trainer
  }

  @SerialVersionUID(1L)
  class TrainStage2[In1 <: IFeature, In2 <: IFeature, Model <: IModel](val trainer: GenericTrain2[In1, In2, Model],
    var source1: SourceComponent[In1],
    var source2: SourceComponent[In2])
    extends TrainComponent[Model] with Serializable {
    
    def getModel() : Option[Model] = cacheModel
    def setModel(model : Option[Model]) : Unit = {
      cacheModel = model
    }
    
    override def accept(v: Visitor) = v.visit(this)
    override def toString = "Train Stage 2-" + trainer
  }

  /*@SerialVersionUID(1L)
  class BasicIndexStage[In <: IFeature, Index <: IIndex]
  (val indexer: GenericBasicIndex[In, Index], val source: SourceComponent[In]) extends Vertex with Serializable {

    var isDirty = false
    var cacheIndex: Option[Index] = None
    
    def setIndex(index : Option[Index]) : Unit = {
      cacheIndex = index
    }
    
    override def accept(v: Visitor) = v.visit(this)
   
    override def toString = "Basic Index Stage-" + indexer
  }*/
  
  @SerialVersionUID(1L)
  class IndexStage[In <: IFeature, Index <: IIndex]
  (val indexer: GenericIndex[In, Index], val source: List[SourceComponent[In]]) extends Vertex with Serializable {

    var isDirty = false
    var cacheIndex: Option[Index] = None
    
    def setIndex(index : Option[Index]) : Unit = {
      cacheIndex = index
    }
    
    override def accept(v: Visitor) = v.visit(this)
   
    override def toString = "Index Stage-" + indexer
  }
  
  @SerialVersionUID(1L)
  class HistogramIndexStage[In <: IFeature, Index <: BasicIndex]
  (val indexer: GenericBasicIndex[In, Index], val source: SourceComponent[In]) extends Vertex with Serializable {

    var isDirty = false
    var cacheIndex: Option[Index] = None
    
    def setIndex(index : Option[Index]) : Unit = {
      cacheIndex = index
    }
    
    override def accept(v: Visitor) = v.visit(this)
   
    override def toString = "Index Stage-" + indexer
  }

  @SerialVersionUID(1L)
  class NaiveIndexQueryStage[In <: IFeature, Out <: IFeature, Index <: BasicIndex]
   (val query: GenericProjWithBasicIndex[In, Out, Index], 
    val source: SourceComponent[In], 
    val index: HistogramIndexStage[Out, Index]) extends Vertex with Serializable {

    var isDirty = false

    def accept(v: Visitor) = v.visit(this)

    override def toString = "Naive Index Query Stage-" + query
  }
  
  @SerialVersionUID(1L)
  class LuceneQueryStage[In <: IFeature, Out <: IFeature, Index <: IIndex, Compose <: ICompose]
   (val query: GenericProjWithIndex[In, Out, Index], 
    val source: SourceComponent[In], 
    val index: IndexStage[In, Index], 
    val compose: GenericCompose[In, Compose]) extends Vertex with Serializable {

    var isDirty = false

    def accept(v: Visitor) = v.visit(this)

    override def toString = "Lucene Query Stage-" + query
  }
}