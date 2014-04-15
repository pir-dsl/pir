package edu.uwm.cs.pir.graph

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.misc.Constants._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.compile.Visitor

import org.apache.spark.rdd._
import scala.reflect.ClassTag

//class Key (id: String, implicit val value: Float) extends Ordered[Key] {
//  def compare(key : Key) = value.compareTo(key.value)
//  override def toString(): String = "(" + id + "=" + value + ")";
//}

object Source {

  trait SourceComponent[Out <: IFeature] extends Vertex {

    def printIds: Unit = {
      collect.map(elem => println(elem.getId()))
    }
    
    def collect: List[Out] = {
      if (cache == None) {
    	  this.accept(GLOBAL_STRATEGY)
      }
      
      if (result == null) {
        log("list from cache") ("info")
        cache.get.collect.toList
      }
      else {
        log("list from result") ("info")
        result.toList
      }
    }
    
    def filter(func: Out => Boolean) (implicit c: ClassTag[Out]) : SourceComponent[Out] = {
      new FilterPipe[Out](this, func)(c)
    }

    def sort(order: String) (implicit c: ClassTag[Out]) : SourceComponent[Out] = {
      new SortPipe[Out](this, order)(c)
    }

    def connect[NewOut <: IFeature: ClassTag](stage: ProjComponent[Out, NewOut]): SourceComponent[NewOut] = {
      new SourcePipe[Out, NewOut](this, stage)
    }

    //    def connect[NewOut <: IFeature](stage: ProjStage[Out, NewOut]): SourceComponent[NewOut] = {
    //      new SourcePipe[Out, NewOut](this, stage)
    //    }

    def connect[NewOut <: IFeature, Indexer <: IIndexer, Index <: IIndex, Compose <: ICompose](query: GenericLuceneQuery, index: IndexStage[Out, Index], compose: GenericCompose[Out, Compose]) = {
      new LuceneQueryStage[Out, NewOut, Index, Compose](query.asInstanceOf[GenericProjWithIndex[Out, NewOut, Index]],
        this, index, compose)
    }
    
    //The 3rd parameter q is not used at all, it's there to remove the compilation error that's caused by confusion the compiler encountered
    def connect[NewOut <: IFeature, Index <: BasicIndex](query: GenericNaiveIndexQuery, index: HistogramIndexStage[NewOut, Index], q: SourceComponent[NewOut]) = {
      new NaiveIndexQueryStage[Out, NewOut, Index](query.asInstanceOf[GenericProjWithIndex[Out, NewOut, Index]], this, index)
    }
    
    /*def connect[NewOut <: IFeature, Index <: BasicIndex](pair: (GenericProjWithIndex[Out, NewOut, Index], HistogramIndexStage[NewOut, Index])) = {
      new NaiveIndexQueryStage[Out, NewOut, Index](pair._1.asInstanceOf[GenericProjWithIndex[Out, NewOut, Index]], this, pair._2)
    }*/

    //    def connect[NewOut <: IFeature, Model <: IModel] (stage: ProjWithModelStage[Out, NewOut, Model]): SourceComponent[NewOut] = {
    //      new SourcePipe[Out, NewOut](this, stage)
    //    }

    var isDirty = false
    var cache: Option[RDD[Out]] = None
    var result : Array[Out] = null
    //var cache : Option[List[Out]] = None

  }

  class SourcePipe[In <: IFeature, Out <: IFeature: ClassTag](val left: SourceComponent[In], val right: ProjComponent[In, Out])
    extends SourceComponent[Out] with Serializable {

    override def accept(v: Visitor) = v.visit(this)

    override def toString(): String = " -> "
  }

  class FilterPipe[In <: IFeature](val left: SourceComponent[In], val right: In => Boolean) (implicit c: ClassTag[In]) 
    extends SourceComponent[In] with Serializable {

    override def accept(v: Visitor) = v.visit(this)

    override def toString(): String = " ~f "
  }
  
  class SortPipe[In <: IFeature](val left: SourceComponent[In], val order : String) (implicit c: ClassTag[In]) 
    extends SourceComponent[In] with Serializable {

    override def accept(v: Visitor) = v.visit(this, if ("ascending" == order) true else false)

    override def toString(): String = " <f< "
  }


}