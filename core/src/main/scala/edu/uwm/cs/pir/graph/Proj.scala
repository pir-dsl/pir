package edu.uwm.cs.pir.graph

import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.pir.compile.Visitor
import edu.uwm.cs.pir.misc.Utils._

object Proj {
  
  trait ProjComponent[In <: IFeature, Out <: IFeature] extends Serializable {
    
    def accept(v: Visitor)
    
    def connect[NewOut <: IFeature](stage: ProjComponent[Out, NewOut]): ProjComponent[In, NewOut] 
           = new ProjPipe[In, Out, NewOut](this, stage)
	
    def apply(f : In) : Out
  }

  @SerialVersionUID(1L)
  case class ProjPipe[In <: IFeature, Middle <: IFeature, Out <: IFeature]
                     (left: ProjComponent[In, Middle], 
                      right: ProjComponent[Middle, Out]) 
                     extends ProjComponent[In, Out] {
    
    override def accept(v:Visitor) = v.visit(this)
 
    def apply(f: In) : Out = {
      log("right = " + right + ", left = " + left + ", f = " + f)
      right.apply(left.apply(f))
    }
  }

}

