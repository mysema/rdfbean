package com.mysema.rdfbean.scala;

import java.util.Collection
import javax.annotation.Nullable
import com.mysema.query.types.{ Expression, ConstantImpl, Ops, Order, OrderSpecifier, ParamExpression, Visitor }
import com.mysema.query.scala._
import com.mysema.rdfbean.model.NODE
import com.mysema.query.scala.{ Operations, SimpleExpression }
import com.mysema.rdfbean.model.{ Blocks, NODE, ID, LIT, BID, UID, RDF }

import scala.collection.JavaConversions._

class QResource[T <: ID](c: Class[T], name: String) extends QNODE[T](c, name) {
    
  def a(t: AnyRef) = Blocks.pattern(this, RDF.`type`, t)
  
  def a(t: AnyRef, c: AnyRef) = Blocks.pattern(this, RDF.`type`, t, c)

  def has(p: AnyRef, o: AnyRef) = Blocks.pattern(this, p, o)
  
  def has(p: AnyRef, o: AnyRef, c: AnyRef) = Blocks.pattern(this, p, o, c)    
}

class QID(name: String) extends QResource[ID](classOf[ID], name) { }

class QUID(name: String) extends QResource[UID](classOf[UID], name) { }

class QBID(name: String) extends QResource[BID](classOf[BID], name) { }