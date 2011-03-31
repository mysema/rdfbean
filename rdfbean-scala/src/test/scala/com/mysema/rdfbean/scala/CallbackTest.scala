package com.mysema.rdfbean.scala

import com.mysema.rdfbean.model.{ NODE, UID, BID, LIT, XSD, MiniRepository, RDFConnection, RDFConnectionCallback }
import com.mysema.rdfbean.`object`.{ DefaultConfiguration, Session, SessionFactoryImpl, SessionUtil, SessionCallback }

import Conversions._

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

import Annotations._

class CallbackTest {
    
  @ClassMapping
  class Person {
    @Id var id: String = _;
  
    @Predicate var firstName: String = _;
  
    @Predicate var lastName: String = _;
  }
  
  @Test
  def SessionCallback{
    val sessionFactory = new SessionFactoryImpl()
    sessionFactory.setConfiguration(new DefaultConfiguration("test:test", classOf[Person]))
    sessionFactory.setRepository(new MiniRepository())
    
    sessionFactory.execute( (s: Session) => {
     s.clear()
    });
  }
  
  @Test
  def SessionCallback_With_Return_Value{
    val sessionFactory = new SessionFactoryImpl()
    sessionFactory.setConfiguration(new DefaultConfiguration("test:test", classOf[Person]))
    sessionFactory.setRepository(new MiniRepository())
    
    val p = sessionFactory.execute( (s: Session) => {
        val p = new Person()
        s.save(p)
        p
    });
    
    assertNotNull(p)
  }
  
  @Test
  def RDFConnectionCallback {
    val repository = new MiniRepository()
    
    repository.execute( (c: RDFConnection) => {
      c.update(null, null)    
    });
  }
    
}

