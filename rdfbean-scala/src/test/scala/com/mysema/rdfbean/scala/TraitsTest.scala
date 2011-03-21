package com.mysema.rdfbean.scala

import com.mysema.rdfbean.TEST
import com.mysema.rdfbean.model.{ RDFS }
import com.mysema.rdfbean.`object`.Session
import com.mysema.rdfbean.`object`.SessionUtil

import Annotations._

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

class TraitsTest {
    
  var session: Session = _
  
  var doc: Document = _
  
  @Before
  def setUp {
    session = SessionUtil.openSession(
            classOf[Document],classOf[Identifiable],classOf[Labeled],classOf[Commented])        
    doc = new Document()
    doc.path = "/abc/def.xml"
    doc.label = "Hello"
    doc.comment = "World"
    
    session.save(doc);    
    session.clear();
        
  }
    
  @Test
  def Save_with_Traits {        
    doc = session.getById(doc.id, classOf[Document])
    assertEquals("/abc/def.xml", doc.path)
    assertEquals("Hello", doc.label)
    assertEquals("World", doc.comment)
  }
  
  @Test
  def Find_Instances_with_Trait_types {
    assertEquals(1, session.findInstances(classOf[Document]).size)
    assertEquals(1, session.findInstances(classOf[Identifiable]).size)
    assertEquals(1, session.findInstances(classOf[Labeled]).size)
    assertEquals(1, session.findInstances(classOf[Commented]).size)
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


