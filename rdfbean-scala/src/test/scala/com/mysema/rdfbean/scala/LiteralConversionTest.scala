package com.mysema.rdfbean.scala

import com.mysema.rdfbean.model.{ NODE, UID, BID, LIT, XSD, MiniRepository, RDFConnection }

import Conversions._

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

import Annotations._

class LiteralConversionTest {
    
  @Test  
  def Conversion {
    val repository = new MiniRepository()
    val connection = repository.openConnection()
    
    connection.findStatements(null, null, "XXX", null, false)
    connection.findStatements(null, null, 1,     null, false)
  }
    
}

