package edu.uwm.cs.pir.strategy

import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.compile.Visitor
import edu.uwm.cs.pir.graph.Source.SourcePipe
import edu.uwm.cs.pir.graph.Source.SortPipe
import edu.uwm.cs.pir.graph.Source.FilterPipe
import edu.uwm.cs.pir.misc.Utils._
import edu.uwm.cs.pir.misc._

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.model._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.feature.lire._
import edu.uwm.cs.mir.prototypes.composer.ICompose
import edu.uwm.cs.mir.prototypes.composer._

import scala.collection.JavaConverters._
import java.util.concurrent.{ Callable, Executors }
import java.util.ArrayList

import org.apache.spark.SparkContext
import SparkContext._
import org.apache.spark.rdd._
import edu.uwm.cs.pir.spark.SparkObject._

import scala.reflect.ClassTag

object Strategy {

  trait RunStrategy extends Visitor {
    val mergeOp: Boolean = false

    //Below we need to provide a type bound ClassTag for Out, same for a few other visit methods
    override def visit[In <: IFeature, Out <: IFeature: ClassTag](q: LoadStage[In, Out]) = {
      if (q.cache == None) {
        val fileListLoaded = q.load.fileList.map(file => q.load.apply(file).get)
        q.cache = Some(sparkContext.parallelize(fileListLoaded))
      }
    }

    override def visit[In <: IFeature, Out <: IFeature: ClassTag](pipe: SourcePipe[In, Out]) {
      if (pipe.cache == None) {
        pipe.left.accept(this)
        val left = pipe.left.cache match {
          case Some(d) => d
          case None => null
        }
        pipe.right.accept(this)

        val result = for {
          elem <- left.toArray
          //elem <- left
        } yield {
          pipe.right.apply(elem)
        }
        pipe.cache = Some(sparkContext.parallelize(result))
        //pipe.cache = Some(result)
      }
    }

    override def visit[In <: IFeature: ClassTag](pipe: SortPipe[In], order: Boolean) {
      if (pipe.cache == None) {
        pipe.left.accept(this)
        val left = pipe.left.cache match {
          case Some(d) => d
          case None => null
        }
        val first = left.first
        first match {
          case a: LireDistanceFeatureAdaptor =>
          case _ => throw new RuntimeException("Sort cannot be performed for feature type: " + first)
        }
        val sorted = left.toArray.sortWith((e1, e2) =>
          if (order) {
            e1.asInstanceOf[LireDistanceFeatureAdaptor].getDistance() <=
              e2.asInstanceOf[LireDistanceFeatureAdaptor].getDistance()
          } else {
            e1.asInstanceOf[LireDistanceFeatureAdaptor].getDistance() >
              e2.asInstanceOf[LireDistanceFeatureAdaptor].getDistance()
          })
        pipe.result = sorted
        pipe.cache = Some(sparkContext.parallelize(sorted))
      }
    }

    override def visit[In <: IFeature: ClassTag](pipe: FilterPipe[In]) {
      if (pipe.cache == None) {
        pipe.left.accept(this)
        val left = pipe.left.cache match {
          case Some(d) => d
          case None => null
        }

        val result = for {
          elem <- left.toArray
          evalRes = pipe.right(elem)
          if (evalRes)
        } yield {
          elem
        }
        pipe.cache = Some(sparkContext.parallelize(result.toArray[In]))
        //pipe.cache = Some(result)
      }
    }

    override def visit[In <: IFeature, Middle <: IFeature, Out <: IFeature](pipe: ProjPipe[In, Middle, Out]) {
      pipe.left.accept(this)
      pipe.right.accept(this)
    }

    override def visit[In <: IFeature, Out <: IFeature](proj: ProjStage[In, Out]) {
      // nothing to do here
    }

    override def visit[In <: IFeature, Out <: IFeature, Model <: IModel](proj: ProjWithModelStage[In, Out, Model]) {
      proj.train.accept(this)
      if (proj.proj.model == None) proj.proj.setModel(
        proj.train.getModel() match {
          case Some(d) => d
          case None => null
        })
    }

    override def visit[In <: IFeature, Out <: IFeature, Index <: IIndex](query: NaiveIndexQueryStage[In, Out, Index]) {
      query.source.accept(this)
      query.index.accept(this)

      val left = (query.index.asInstanceOf[SourcePipe[In, IFeature]]).left
      val right = (query.index.asInstanceOf[SourcePipe[IFeature, Out]]).right
      val queryFeature = query.source.cache.get.first
      //val queryFeature = right.apply(left.asInstanceOf[SourcePipe[In, IFeature]].right.apply(elem))

      query.query.setIndex(query.index.cacheIndex.get)
      val queryResult = query.query.asInstanceOf[GenericNaiveIndexQuery].apply(queryFeature)
      log(queryResult.printResult)
    }

    override def visit[In <: IFeature, Out <: IFeature, Index <: IIndex, Compose <: ICompose](query: LuceneQueryStage[In, Out, Index, Compose]) {
      //The below source is a query source
      query.source.accept(this)
      query.index.accept(this)

      //query.asInstanceOf[GenericLuceneQuery].setIndex(query.index.cacheIndex.get)
      var fs: List[IFeature] = List()

      val queryFeatureList = for {
        sourcePipe <- query.index.source
      } yield {
        // A query source has only one data item in the List;
        // The below code applies the projectors that are used for the indexed features to the 
        // query data item
        val left = (sourcePipe.asInstanceOf[SourcePipe[In, IFeature]]).left
        val right = (sourcePipe.asInstanceOf[SourcePipe[IFeature, Out]]).right
        val elem = query.source.cache.get.first
        right.apply(left.asInstanceOf[SourcePipe[In, IFeature]].right.apply(elem))
      }
      query.query.setIndex(query.index.cacheIndex.get)
      val queryResult = for {
        queryFeature <- queryFeatureList
      } yield {
        query.query.asInstanceOf[GenericLuceneQuery].apply(queryFeature) :: Nil
      }
      val modified = queryResult.map(list => list.map(elem => elem.asInstanceOf[IFeature]).asJava).asJava
      val weights = query.compose.asInstanceOf[GenericLuceneCompose[Image, LuceneWeightedQueryResult]].weights.map(weight => weight.asInstanceOf[java.lang.Double])
      val luceneComposer = new LuceneWeightedComposer(weights.asJava)
      val res = luceneComposer.apply(modified)
      log(res.asInstanceOf[LuceneWeightedQueryResult].printResults())
    }

    override def visit[In <: IFeature, Model <: IModel](train: TrainStage[In, Model]) = {
      if (train.getModel == None) {
        time(trainFunc1(train))("" + train.trainer)
      }

    }

    def trainFunc1[In <: IFeature, Model <: IModel](train: TrainStage[In, Model]): Unit = {
      train.source.accept(this)
      var fs = (train.source.cache match {
        case Some(d) => d
        case None => null
      }) :: Nil

      log("train data with " + train.trainer + "\n")
      val newFs = fs.map(elem => elem.toArray.toList)
      train.setModel(Some(train.trainer.apply(newFs)))

    }

    override def visit[In1 <: IFeature, In2 <: IFeature, Model <: IModel](train2: TrainStage2[In1, In2, Model]) = {
      if (train2.getModel == None) {

        time(trainFunc2(train2))("" + train2.trainer)
      }
    }

    def trainFunc2[In1 <: IFeature, In2 <: IFeature, Model <: IModel](train2: TrainStage2[In1, In2, Model]): Unit = {
      var fs: List[List[IFeature]] = List(Nil)

      train2.source1.accept(this)
      train2.source2.accept(this)

      val x1 = train2.source1.cache match {
        case Some(d) => d
        case None => null
      }
      val x2 = train2.source2.cache match {
        case Some(d) => d
        case None => null
      }

      //Just do this to keep the right type
      fs = List(x1.toArray.toList, x2.toArray.toList)

      log("train data with " + train2.trainer + "\n")
      train2.setModel(Some(train2.trainer.apply(fs)))
    }

    override def visit[In <: IFeature, Index <: IIndex](index: IndexStage[In, Index]) = {
      if (index.cacheIndex == None) {
        time(indexFunc(index, this))("" + index.indexer)
      }
      index.cacheIndex
    }

    override def visit[In <: IFeature, Index <: IIndex](index: HistogramIndexStage[In, Index]) = {
      if (index.cacheIndex == None) {
        time(basicIndexFunc(index, this))("" + index.indexer)
      }
      index.cacheIndex
    }

  }

  def basicIndexFunc[In <: IFeature, Index <: IIndex](index: HistogramIndexStage[In, Index], strategy: RunStrategy): Unit = {
    var fs: List[IFeature] = Nil

    index.source.accept(strategy)

    fs = index.source.cache match {
      case Some(d) => d.toArray.toList
      case None => null
    }

    log("index data with " + index.indexer + "\n")
    index.setIndex(Some(index.indexer.apply(fs.asInstanceOf[List[In]])))
  }

  def indexFunc[In <: IFeature, Index <: IIndex](index: IndexStage[In, Index], strategy: RunStrategy): Unit = {
    var fs: List[List[IFeature]] = List(Nil)

    index.source.foreach(elem => {
      elem.accept(strategy);
      fs = fs :+ (elem.cache match {
        //Just do this to keep the right type
        case Some(d) => d.toArray.toList
        case None => null
      })
    })
    log("index data with " + index.indexer + "\n")
    index.setIndex(Some(index.indexer.apply(fs.asInstanceOf[List[List[In]]])))
  }

  case class SequentialStrategy() extends RunStrategy {
  }

  case class ParallelStrategy(val maxNumberOfThreads: Int) extends RunStrategy {

    override def visit[In <: IFeature, Out <: IFeature: ClassTag](pipe: SourcePipe[In, Out]) {

      if (pipe.cache == None) {

        val availableProcessors: Int = java.lang.Runtime.getRuntime.availableProcessors
        val numProcessorUsed = if (maxNumberOfThreads < availableProcessors) maxNumberOfThreads else availableProcessors
        log("# of processors used = " + numProcessorUsed)

        pipe.left.accept(this)
        val left = pipe.left.cache match {
          case Some(d) => d
          case None => null
        }

        //Just do this to keep the right type
        val fs: List[In] = left.toArray.toList
        val list = chunkList(fs, availableProcessors)

        pipe.right.accept(this)

        val result = getResultList[In, Out](list, pipe.right, numProcessorUsed)
        pipe.cache = Some(sparkContext.parallelize(result.asInstanceOf[List[Out]]))
      }
    }

    def getResultList[In <: IFeature, Out <: IFeature](list: List[List[In]], proj: ProjComponent[In, Out], numProcessorUsed: Int): List[IFeature] = {

      val pool = Executors.newFixedThreadPool(numProcessorUsed)
      def execute(proj: ProjComponent[In, Out], qP: In): IFeature = {
        val future = pool.submit(new Callable[IFeature] {
          def call(): IFeature = {
            log("Executing function on thread: " + Thread.currentThread.getName)
            proj.apply(qP)
          }
        })
        future.get
      }

      if (list == Nil) Nil
      else {
        val res = for {
          arg <- list.head
        } yield {
          execute(proj, arg)
        }

        pool.shutdown()
        while (!pool.isTerminated()) {}
        res ::: getResultList(list.tail, proj, numProcessorUsed)
      }
    }

  }

  case class SparkStrategy() extends RunStrategy {

    override def visit[In <: IFeature, Out <: IFeature: ClassTag](q: LoadStage[In, Out]) = {
      if (q.cache == None) {
        time(loadFunc(q))("" + q)
      }
    }

    def loadFunc[In <: IFeature, Out <: IFeature: ClassTag](q: LoadStage[In, Out]): Unit = {
      val fileList = sparkContext.parallelize(q.load.fileList, sparkPartitionSize.toInt)
      val result = fileList.map { elem => q.load.apply(elem).get }.persist
      //log("result = " + result.collect)
      q.cache = Some(result)
    }

    override def visit[In <: IFeature, Out <: IFeature: ClassTag](pipe: SourcePipe[In, Out]) {
      if (pipe.cache == None) {
        log("in visit SourcePipe " + pipe)("INFO")
        time(projFunc(pipe, this))("" + pipe.right + " on " + pipe.left)
      } else {
        log("pipe = " + pipe + ", pipe.left = " + pipe.left + ", pipe.right = " + pipe.right)
      }
    }

  }

  def projFunc[In <: IFeature, Out <: IFeature: ClassTag](pipe: SourcePipe[In, Out], strategy: RunStrategy): Unit = {
    pipe.left.accept(strategy)
    val left = pipe.left.cache match {
      case Some(d) => d
      case None => null
    }
    //log ("pipe.left.cache = " + pipe.left.cache)
    pipe.right.accept(strategy)
    //log("pipe.right.cache = " + pipe.right);

    val result = getResultList[In, Out](left, pipe.right)
    //if we use result.collect, we will have stack overflow issue for large dataset
    log("result = " + result.count)
    pipe.cache = Some(result)
  }

  def getResultList[In <: IFeature, Out <: IFeature: ClassTag](rdd: RDD[In], proj: ProjComponent[In, Out]): RDD[Out] = {
    if (rdd == null || rdd == Nil) {
      null
    } else {
      rdd.map { elem => { /*log("the elem = " + elem); */ proj.apply(elem) } }.persist
    }
  }

}