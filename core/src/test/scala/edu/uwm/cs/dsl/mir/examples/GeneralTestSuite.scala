package edu.uwm.cs.dsl.mir.examples

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import common._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.utils._
import edu.uwm.cs.mir.prototypes.proj.lucene._
import edu.uwm.cs.mir.prototypes.composer._
import edu.uwm.cs.mir.prototypes.index._
import edu.uwm.cs.mir.prototypes.utils.Utils._
import edu.uwm.cs.pir.compile.Function._
import edu.uwm.cs.pir.compile.Compile._
import edu.uwm.cs.pir.strategy.Strategy._
import edu.uwm.cs.pir.misc.InputType
import edu.uwm.cs.pir.graph.Stage._
import edu.uwm.cs.pir.graph.Source._
import edu.uwm.cs.pir.compile.Generic.GenericInterface._
import edu.uwm.cs.pir.compile.Generic.impl.GenericImpl._
import edu.uwm.cs.pir.compile.Scope._
import edu.uwm.cs.pir.spark.SparkObject._
import org.scalatest.junit.JUnitRunner

import edu.uwm.cs.mir.prototypes.index._

@RunWith(classOf[JUnitRunner])
class PIRGenericSuite extends FunSuite {
    
  test ("InvertedIndexSearchResultSorting") {
    val array = List(InvertedIndexSearchResult(1, "docId1", "result1", 8.1), 
        InvertedIndexSearchResult(2, "docId2", "result2", 3.5),
    	InvertedIndexSearchResult(3, "docId3", "result3", 4.6),
    	InvertedIndexSearchResult(4, "docId4", "result4", 7.8), 
    	InvertedIndexSearchResult(5, "docId5", "result5", Double.NaN)).sortWith(_>_)
    array.map(elem => println(elem))
  }

  test("InvertedIndex Testing") {
    val index = new InvertedIndex(new Tokenizer)
    index.index("id1", "content1")
    index.index("id2", "content2")
    index.index("id3", "content3")
    
    serializeObject(index, awsS3Config, "testId", true)
    
    val result = deSerializeObject("testId", awsS3Config, true).asInstanceOf[IIndex]
    
    println
  }
}
