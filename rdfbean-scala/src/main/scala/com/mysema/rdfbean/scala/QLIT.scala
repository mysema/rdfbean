package com.mysema.rdfbean.scala;

import java.util.Collection
import javax.annotation.Nullable
import com.mysema.query.types.{ Expression, ConstantImpl, Ops, Order, OrderSpecifier, ParamExpression, Visitor }
import com.mysema.query.scala._
import com.mysema.rdfbean.model.NODE
import com.mysema.query.scala.{ Operations, SimpleExpression }
import com.mysema.rdfbean.model.{ Blocks, NODE, ID, LIT, BID, UID, RDF }

import scala.collection.JavaConversions._

class QLIT(name: String) extends QNODE[LIT](classOf[LIT], name) {

  private def literal(v: String) = new ConstantImpl[LIT](classOf[LIT], new LIT(v))

  private def literal(lit: LIT) = new ConstantImpl[LIT](classOf[LIT], lit)
  
  def <(lit: LIT) = Operations.boolean(Ops.LT, this, literal(lit));
  
  def >(lit: LIT) = Operations.boolean(Ops.GT, this, literal(lit));
  
  def <=(lit: LIT) = Operations.boolean(Ops.LOE, this, literal(lit));
  
  def >=(lit: LIT) = Operations.boolean(Ops.GOE, this, literal(lit));  
  
  def between(lit1: LIT, lit2 :LIT) = Operations.boolean(Ops.BETWEEN, this, literal(lit1), literal(lit2));  
  
  def like(v: String) = Operations.boolean(Ops.LIKE, this, literal(v))
  
  def matches(v: String) = Operations.boolean(Ops.MATCHES, this, literal(v))
  
  def eqIgnoreCase(v: String) = Operations.boolean(Ops.EQ_IGNORE_CASE, this, literal(v))
  
  lazy val empty = Operations.boolean(Ops.STRING_IS_EMPTY, this); 

}
