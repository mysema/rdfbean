package com.mysema.rdfbean.scala;

import java.util.Collection
import javax.annotation.Nullable
import com.mysema.query.types.{ Expression, ConstantImpl, Ops, Order, OrderSpecifier, ParamExpression, Visitor }
import com.mysema.query.scala._
import com.mysema.rdfbean.model.NODE
import com.mysema.query.scala.{ Operations, SimpleExpression }
import com.mysema.rdfbean.model.{ Blocks, NODE, ID, LIT, BID, UID, RDF }

//import scala.collection.JavaConversions._

object QNODE {

  val s = new QID("s")

  val p = new QID("p")

  val o = new QNODE[NODE](classOf[NODE], "o")

  val c = new QID("c")

  val t = new QID("type")

  val typeContext = new QID("typeContext")

  val first = new QNODE[NODE](classOf[NODE], "first")

  val rest = new QID("rest")

}

class QNODE[T <: NODE](val t: Class[T], val name: String) extends ParamExpression[T] with SimpleExpression[T] {

  def accept[R, C](v: Visitor[R, C], context: C): R = v.visit(this, context)

  def getName() = name

  def getType() = t

  def isAnon() = false

  def getNotSetMessage() = "A parameter of type " + getType.getName + " was not set"

  def is(p: AnyRef, s: AnyRef) = Blocks.pattern(s, p, this)

  lazy val id = new QID(getName)

  lazy val lit = new QLIT(getName)

  lazy val asc = new OrderSpecifier[String](Order.ASC, this.asInstanceOf[Expression[String]])

  lazy val desc = new OrderSpecifier[String](Order.DESC, this.asInstanceOf[Expression[String]])

  lazy val stringValue = Operations.string(Ops.STRING_CAST, this)

  override def equals(o: Any): Boolean = {
    o match {
      case n: QNODE[_] => n.getName == name
      case _ => false
    }
  }

  override def hashCode = getName.hashCode

}

