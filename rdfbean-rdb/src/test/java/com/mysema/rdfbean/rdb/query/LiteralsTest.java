package com.mysema.rdfbean.rdb.query;

import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.*;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.rdb.AbstractRDBTest;

public class LiteralsTest extends AbstractRDBTest{
        
    @ClassMapping(ns=TEST.NS)
    public static class Literals {
        
        @Id
        private String id;
        
        @Predicate
        private int intValue;
        
        @Predicate
        private long longValue;
        
        @Predicate
        private double doubleValue;
        
        @Predicate
        private float floatValue;
        
        @Predicate
        private byte byteValue;
        
        @Predicate
        private short shortValue;
        
        @Predicate
        private Date dateValue;
        
        @Predicate
        private String stringValue;
        
        @Predicate
        private boolean booleanValue;

        public String getId() {
            return id;
        }

        public int getIntValue() {
            return intValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }
        
        public float getFloatValue() {
            return floatValue;
        }

        public byte getByteValue() {
            return byteValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public Date getDateValue() {
            return dateValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }
                
    }
    
    private Session session;
    
    @Before
    public void setUp(){
        session = SessionUtil.openSession(repository, Literals.class);
        session.setFlushMode(FlushMode.ALWAYS);        
    }
    
    @After
    public void tearDown() throws IOException{
        if (session != null){
            session.close();
        }
    }
    
    @Test
    public void test(){
       Literals literals = new Literals();
       literals.byteValue = (byte)1;
       literals.dateValue = new Date();
       literals.doubleValue = 2.0;
       literals.floatValue = (float)3.0;
       literals.intValue = 4;
       literals.longValue = 5l;
       literals.shortValue = (short)6;
       literals.stringValue = "7";        
       session.save(literals);
        
       Literals l = Alias.alias(Literals.class);
       assertEquals(Byte.valueOf(literals.getByteValue()), session.from($(l)).uniqueResult($(l.getByteValue())));
//       assertEquals(literals.getDateValue(), session.from($(l)).uniqueResult($(l.getDateValue())));
       assertEquals(Double.valueOf(literals.getDoubleValue()), session.from($(l)).uniqueResult($(l.getDoubleValue())));
       assertEquals(Float.valueOf(literals.getFloatValue()), session.from($(l)).uniqueResult($(l.getFloatValue())));
       assertEquals(Integer.valueOf(literals.getIntValue()), session.from($(l)).uniqueResult($(l.getIntValue())));
       assertEquals(Long.valueOf(literals.getLongValue()), session.from($(l)).uniqueResult($(l.getLongValue())));
//       assertEquals(Short.valueOf(literals.getShortValue()), session.from($(l)).uniqueResult($(l.getShortValue())));
       assertEquals(literals.getStringValue(), session.from($(l)).uniqueResult($(l.getStringValue())));
       
    }

}
