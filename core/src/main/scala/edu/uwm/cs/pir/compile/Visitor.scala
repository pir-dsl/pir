package edu.uwm.cs.pir.compile
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.index._
import scala.collection.mutable.Queue
import scala.reflect.ClassTag
import edu.uwm.cs.pir.graph.Vertex

class Visitor {
      def visit[In <: IFeature, Out <: IFeature  : ClassTag] (load: LoadStage[In, Out]) {}
      def visit[In <: IFeature, Out <: IFeature : ClassTag] (pipe: SourcePipe[In, Out]) {}
      def visit[In <: IFeature: ClassTag] (pipe: FilterPipe[In]) {}
      def visit[In <: IFeature: ClassTag] (pipe: SortPipe[In], order : Boolean) {}
      def visit[In <: IFeature, Middle <:IFeature, Out <: IFeature] (pipe: ProjPipe[In, Middle, Out]) {}
      def visit[In <: IFeature, Out <: IFeature] (proj: ProjStage[In, Out]) {}
      def visit[In <: IFeature, Out <: IFeature, Model <: IModel] (proj: ProjWithModelStage[In, Out, Model]) {}
      def visit[In <: IFeature, Model <: IModel] (train: TrainStage[In, Model]) {}
      def visit[In <: IFeature, In2 <:IFeature, Model <: IModel] (train: TrainStage2[In, In2, Model]) {}
      def visit[In <: IFeature, Index <: IIndex] (index: IndexStage[In, Index]) {}
      def visit[In <: IFeature, Index <: BasicIndex] (index: HistogramIndexStage[In, Index]) {}
      def visit[In <: IFeature, Out <: IFeature, Index <: IIndex, Compose <: ICompose] 
      (query: LuceneQueryStage[In, Out, Index, Compose]) {}
      def visit[In <: IFeature, Out <: IFeature, Index <: IIndex, Compose <: ICompose] 
      (query: NaiveIndexQueryStage[In, Out, Index]) {}     
}

class JobVisitor extends Visitor {
      val queue : Queue[Vertex] = new Queue[Vertex]
  
      override def visit[In <: IFeature, Out <: IFeature : ClassTag] (load: LoadStage[In, Out]) {
    	  if (!queue.contains(load)) queue.enqueue(load)
      }
      
      override def visit[In <: IFeature, Out <: IFeature : ClassTag] (pipe: SourcePipe[In, Out]) {
          pipe.left.accept(this)
          pipe.right.accept(this)      
          if (!queue.contains(pipe)) queue.enqueue(pipe)
      }

      override def visit[In <: IFeature, Middle <:IFeature, Out <: IFeature] (pipe: ProjPipe[In, Middle, Out]) {}

      override def visit[In <: IFeature, Out <: IFeature] (proj: ProjStage[In, Out]) {
//          if (!queue.contains(proj)) queue.enqueue(proj)
      }

      override def visit[In <: IFeature, Out <: IFeature, Model <: IModel] (proj: ProjWithModelStage[In, Out, Model]) {
          proj.train.accept(this)
//          if (!queue.contains(proj)) queue.enqueue(proj)
      }

      override def visit[In <: IFeature, Model <: IModel] (train: TrainStage[In, Model]) {
          train.source.accept(this)
          if (!queue.contains(train)) queue.enqueue(train)
      }

      override def visit[In <: IFeature, In2 <:IFeature, Model <: IModel] (train: TrainStage2[In, In2, Model]) {
          train.source1.accept(this)
          train.source2.accept(this)
          if (!queue.contains(train)) queue.enqueue(train)
      }
      
      override def visit[In <: IFeature, Index <: IIndex] (index: IndexStage[In, Index]) {
          index.source.foreach(elem => elem.accept(this))
          if (!queue.contains(index)) queue.enqueue(index)
      }
      
      override def visit[In <: IFeature, Out <: IFeature, Index <: IIndex, Compose <: ICompose] 
      (query: LuceneQueryStage[In, Out, Index, Compose]) {   
          query.index.accept(this)
          query.source.accept(this)
          if (!queue.contains(query)) queue.enqueue(query)
      }
}