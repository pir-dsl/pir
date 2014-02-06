package edu.uwm.cs.dsl.mir.examples

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import common._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.utils._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.pir.compile.Function._
import edu.uwm.cs.pir.compile.Compile._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.misc.InputType
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Scope._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DSLPrototypeSuite extends FunSuite {

  val img = load[Image](Constants.WIKIPEDIA_IMAGES_ROOT, InputType.IMAGE)
  val qImg = load[Image](Constants.WIKIPEDIA_ROOT + "/query.jpg", InputType.IMAGE)
  val idx = index(f_luceneIdx, img.connect(f_cedd).connect(f_luceneDocTransformer), img.connect(f_fcth).connect(f_luceneDocTransformer))
  val q = query(f_weightedQuery, idx, qImg)
  
  test("Connectivity test") {
    assert(q != null)
  }
}
