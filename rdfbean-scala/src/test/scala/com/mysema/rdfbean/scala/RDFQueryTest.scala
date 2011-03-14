package com.mysema.rdfbean.scala

import com.mysema.rdfbean.TEST
import com.mysema.rdfbean.model.{ MiniRepository, RDFQueryImpl, QID, QLIT, QNODE, RDFS, NODE, UID, BID, LIT };

import org.junit.{ Ignore, Test, Before, After };
import org.junit.Assert._;

import RDFQueryTest._
import Conversions._

class RDFQueryTest {
    
  val repository = new MiniRepository();  
    
  val query = new RDFQueryImpl(repository.openConnection());
  
  @Test
  def User_a_User_with_Label {        
    query 
      .where ( user a User, user has (RDFS.label, label)) 
      .select ( user, label );
  }
  
  @Test
  def User_in_Resources {    
    query 
      .where ( user in (res1, res2), user has (pred, obj) )
      .select ( user, pred, obj );
  }
  
  def Label_is {
    query 
      .where ( label eq "XXX", label is (RDFS.label, user) )
      .select ( user );      
  }
  
}

object RDFQueryTest {
  // resources
  val res1 = new BID();
  val res2 = new BID();  
  val User = new UID(TEST.NS, "User");
  
  // variables
  val user = new QID("user");
  val label = new QLIT("label");
  val pred = new QID("pred");
  val obj = new QNODE[NODE](classOf[NODE], "obj");
}
