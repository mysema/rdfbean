/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.LiteralsDomain;
import com.mysema.rdfbean.domains.LiteralsDomain.Literals;
import com.mysema.rdfbean.testutil.SessionConfig;
import com.mysema.rdfbean.virtuoso.AbstractConnectionTest;

@SessionConfig(Literals.class)
public class LiteralsTest extends AbstractConnectionTest implements LiteralsDomain{
    
    @Test
    public void test(){
       Literals literals = new Literals();
       literals.booleanValue = true;
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
       assertEquals(Boolean.valueOf(literals.isBooleanValue()), session.from($(l)).uniqueResult($(l.isBooleanValue())));
       assertEquals(Byte.valueOf(literals.getByteValue()), session.from($(l)).uniqueResult($(l.getByteValue())));
       assertEquals(literals.getDateValue(), session.from($(l)).uniqueResult($(l.getDateValue())));
       assertEquals(Double.valueOf(literals.getDoubleValue()), session.from($(l)).uniqueResult($(l.getDoubleValue())));
       assertEquals(Float.valueOf(literals.getFloatValue()), session.from($(l)).uniqueResult($(l.getFloatValue())));
       assertEquals(Integer.valueOf(literals.getIntValue()), session.from($(l)).uniqueResult($(l.getIntValue())));
       assertEquals(Long.valueOf(literals.getLongValue()), session.from($(l)).uniqueResult($(l.getLongValue())));
       assertEquals(Short.valueOf(literals.getShortValue()), session.from($(l)).uniqueResult($(l.getShortValue())));
       assertEquals(literals.getStringValue(), session.from($(l)).uniqueResult($(l.getStringValue())));
       
    }
    
    @Test
    public void Boolean(){
        Literals literals = new Literals();
        literals.booleanValue = true;
        session.save(literals);
        session.flush();
        session.clear();
        
        // load
        literals = session.get(Literals.class, literals.getId());
        assertTrue(literals.isBooleanValue());
        
        // change
        literals.booleanValue = false;
        session.save(literals);
        session.flush();
        session.clear();
        
        // reload
        literals = session.get(Literals.class, literals.getId());
        assertFalse(literals.isBooleanValue());
    }

}
