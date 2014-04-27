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
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.compile.Scope._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PIRGenericSuite extends FunSuite {
    
  test ("InvertedIndexSearchResultSorting") {
    val array = List(InvertedIndexSearchResult(1, "docId1", "result1", 8.1), 
        InvertedIndexSearchResult(2, "docId1", "result1", 3.5),
    	InvertedIndexSearchResult(3, "docId1", "result1", 4.6),
    	InvertedIndexSearchResult(4, "docId1", "result1", 7.8)).toSeq.sorted
    array.map(elem => println(elem))
  }

}