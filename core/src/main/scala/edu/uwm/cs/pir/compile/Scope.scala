package edu.uwm.cs.pir.compile

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.model._

import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.graph.Proj._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._

import scala.reflect.ClassTag

object Scope {

  implicit def loadToSourceStage[In <: IFeature, Out <: IFeature : ClassTag]
  (load: GenericLoad[Out]) = new LoadStage[In, Out](load)

  implicit def projToProjStage[In <: IFeature, Out <: IFeature]
  (proj: GenericProj[In, Out]) = new ProjStage(proj)
  
  implicit def projWithModelToProjWithModelStage[In <: IFeature, Out <: IFeature, Model <: IModel] 
  (pair: (GenericProjWithModel[In, Out, Model], TrainComponent[Model])) = new ProjWithModelStage(pair._1, pair._2)

}