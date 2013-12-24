package edu.uwm.cs.pir.graph

import edu.uwm.cs.pir.compile.Visitor

trait Vertex {
	def accept(v: Visitor)
}