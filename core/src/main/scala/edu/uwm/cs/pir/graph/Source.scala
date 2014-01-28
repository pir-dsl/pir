package edu.uwm.cs.pir.graph

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.compile.Visitor

import org.apache.spark.rdd._

object Source {

  trait SourceComponent[Out <: IFeature] extends Vertex {

    def filter(func: (Out, Visitor) => Boolean) (implicit c: ClassManifest[Out]) : SourceComponent[Out] = {
      new FilterPipe[Out](this, func)(c)
    }

    def sort(order: String) (implicit c: ClassManifest[Out]) : SourceComponent[Out] = {
      new SortPipe[Out](this, order)(c)
    }

    def connect[NewOut <: IFeature: ClassManifest](stage: ProjComponent[Out, NewOut]): SourceComponent[NewOut] = {
      new SourcePipe[Out, NewOut](this, stage)
    }

    //    def connect[NewOut <: IFeature](stage: ProjStage[Out, NewOut]): SourceComponent[NewOut] = {
    //      new SourcePipe[Out, NewOut](this, stage)
    //    }

    def connect[NewOut <: IFeature, Indexer <: IIndexer, Index <: IIndex, Compose <: ICompose](query: GenericLuceneQuery, index: IndexStage[Out, Index], compose: GenericCompose[Out, Compose]) = {
      new LuceneQueryStage[Out, NewOut, Index, Compose](query.asInstanceOf[GenericProjWithIndex[Out, NewOut, Index]],
        this, index, compose)
    }

    //    def connect[NewOut <: IFeature, Model <: IModel] (stage: ProjWithModelStage[Out, NewOut, Model]): SourceComponent[NewOut] = {
    //      new SourcePipe[Out, NewOut](this, stage)
    //    }

    var isDirty = false
    var cache: Option[RDD[Out]] = None
    //var cache : Option[List[Out]] = None

  }

  class SourcePipe[In <: IFeature, Out <: IFeature: ClassManifest](val left: SourceComponent[In], val right: ProjComponent[In, Out])
    extends SourceComponent[Out] with Serializable {

    override def accept(v: Visitor) = v.visit(this)

    override def toString(): String = " -> "
  }

  class FilterPipe[In <: IFeature](val left: SourceComponent[In], val right: (In, Visitor) => Boolean) (implicit c: ClassManifest[In]) 
    extends SourceComponent[In] with Serializable {

    override def accept(v: Visitor) = v.visit(this)

    override def toString(): String = " ->f "
  }
  
  class SortPipe[In <: IFeature](val left: SourceComponent[In], val order : String) (implicit c: ClassManifest[In]) 
    extends SourceComponent[In] with Serializable {

    override def accept(v: Visitor) = v.visit(this, if ("ascending" == order) true else false)

    override def toString(): String = " ->f "
  }

}