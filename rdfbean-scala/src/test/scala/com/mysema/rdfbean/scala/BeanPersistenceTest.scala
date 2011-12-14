package com.mysema.rdfbean.scala

import com.mysema.rdfbean.TEST
import com.mysema.rdfbean.`object`.Session
import com.mysema.rdfbean.`object`.SessionUtil

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

class BeanPersistenceTest {

  @Test
  def Save {
    val session = SessionUtil.openSession(classOf[Person])
    var person = new Person()
    person.firstName = "John"
    person.lastName = "Smith"
    session.save(person)
    session.clear()
    
    person = session.getById(person.id, classOf[Person])
    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
  }  
    
}

@ClassMapping
class Person {
  @Id var id: String = _;
  
  @Predicate var firstName: String = _;
  
  @Predicate var lastName: String = _;
}