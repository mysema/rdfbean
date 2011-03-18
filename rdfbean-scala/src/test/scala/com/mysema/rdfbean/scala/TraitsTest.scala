package com.mysema.rdfbean.scala

import com.mysema.rdfbean.TEST
import com.mysema.rdfbean.model.{ RDFS }
import com.mysema.rdfbean.`object`.Session
import com.mysema.rdfbean.`object`.SessionUtil

import Annotations._

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

class TraitsTest {
     
  @Test
  def Traits_Save {
    val session = SessionUtil.openSession(
            classOf[Document],
            classOf[Identifiable],
            classOf[Labeled],
            classOf[Commented])
    var doc = new Document()
    doc.path = "/abc/def.xml"
    doc.label = "Hello"
    doc.comment = "World"
    
    session.save(doc);    
    session.clear();
    
    doc = session.getById(doc.id, classOf[Document])
    assertEquals("John", doc.path)
    assertEquals("Smith", doc.label)
    assertEquals("Smith", doc.comment)
  }
     
}

@ClassMapping
class Document extends Identifiable with Labeled with Commented {
    @Predicate var path: String = _
}

@ClassMapping
trait Identifiable {
  @Id var id: String = _  
}

@ClassMapping
trait Labeled {
  @Predicate(ns=RDFS.NS) var label: String = _         
}

@ClassMapping
trait Commented {
  @Predicate(ns=RDFS.NS) var comment: String = _  
}


