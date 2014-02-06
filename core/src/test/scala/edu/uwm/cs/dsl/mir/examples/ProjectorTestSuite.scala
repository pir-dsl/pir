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
class ProjectorTestSuite extends FunSuite {

  val img = new Image(Constants.SAMPLE_IMAGES_ROOT + "test/1000.jpg")
  val colorLayout = f_colorLayout.apply(img)
  val edgeHistogram = f_edgeHistogram.apply(img)
  val gabor = f_gabor.apply(img)

  test("Connectivity test") {
    assert(colorLayout != null)
    assert(edgeHistogram != null)
    assert(gabor != null)
  }
}