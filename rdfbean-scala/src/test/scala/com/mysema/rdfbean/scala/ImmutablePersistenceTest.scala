package com.mysema.rdfbean.scala

import com.mysema.rdfbean.TEST
import com.mysema.rdfbean.`object`.Session
import com.mysema.rdfbean.`object`.SessionUtil

import Annotations._

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

class ImmutablePersistenceTest {

  @Test 
  def Class_Save {
    val session = SessionUtil.openSession(classOf[PersonImmutable])
    var person = new PersonImmutable(null, "John", "Smith")
    session.save(person)
    session.clear()
    
    person = session.getById(person.id, classOf[PersonImmutable])
    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
  }  
    
}

@ClassMapping
class PersonImmutable (
  @Id val id: String,  
  
  @Predicate val firstName: String,
  
  @Predicate val lastName: String){
}