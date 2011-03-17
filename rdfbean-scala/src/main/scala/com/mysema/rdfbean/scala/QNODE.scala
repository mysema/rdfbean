package com.mysema.rdfbean.scala;

import java.util.Collection
import javax.annotation.Nullable
import com.mysema.query.types.{ ConstantImpl, Ops, Order, OrderSpecifier, ParamExpression, Visitor }
import com.mysema.query.scala._
import com.mysema.rdfbean.model.NODE
import com.mysema.query.scala.{ Operations, SimpleExpression }
import com.mysema.rdfbean.model.{ Blocks, NODE, ID, LIT, UID, RDF }

/**
 * @author tiwe
 *
 * @param <T>
 */
class QNODE[T <: NODE](val t: Class[T], val name: String) extends ParamExpression[T] with SimpleExpression[T] {

  def accept[R,C](v: Visitor[R,C], context: C): R = v.visit(this, context)
    
  def getName() = name
    
  def getType() = t
  
  def isAnon() = false
  
  def getNotSetMessage() = "A parameter of type " + getType().getName() + " was not set"
    
  def is(predicate: AnyRef, subject: AnyRef) =  Blocks.pattern(subject, predicate, this)

  lazy val id = new QID(getName)
  
  lazy val lit = new QLIT(getName)
  
  // TODO : asc, desc

  override def in(values: T*): BooleanExpression = {
    values.tail.foldLeft (this === values.head) { (l, r) => l or (this === r) } 
  }

//    public BooleanExpression in(Collection<? extends T> values){

  override def equals(o: Any): Boolean = {
    if (o == this){
      true
    }else if (o.isInstanceOf[QNODE[_]]){
      o.asInstanceOf[QNODE[_]].getName eq getName
    }else{
      false
    }
  }

  override def hashCode = getName.hashCode
  
}

object QNODE {

  val s = new QID("s")
    
  val p = new QID("p")

  val o = new QNODE[NODE](classOf[NODE],"o")

  val c = new QID("c")

  val t = new QID("type")

  val typeContext = new QID("typeContext")

  val first = new QNODE[NODE](classOf[NODE], "first")

  val rest = new QID("rest")

}

class QID(name: String) extends QNODE[ID](classOf[ID], name) {

  def a(t: AnyRef) = Blocks.pattern(this, RDF.`type`, t)

  def has(p: AnyRef, o: AnyRef) = Blocks.pattern(this, p, o)
  
}

class QLIT(name: String) extends QNODE[LIT](classOf[LIT], name) {

  private def literal(v: String) = new ConstantImpl[LIT](classOf[LIT], new LIT(v))

  private def literal(lit: LIT) = new ConstantImpl[LIT](classOf[LIT], lit)

  // TODO : <, >, <=, >=
  
  def like(v: String) = Operations.boolean(Ops.LIKE, this, literal(v))
  
  def matches(v: String) = Operations.boolean(Ops.MATCHES, this, literal(v))
  
  def eqIgnoreCase(v: String) = Operations.boolean(Ops.EQ_IGNORE_CASE, this, literal(v))
  
  lazy val empty = Operations.boolean(Ops.STRING_IS_EMPTY, this); 

}



