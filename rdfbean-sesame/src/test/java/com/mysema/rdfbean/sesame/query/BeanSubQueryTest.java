/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * SubQueryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQueryTest extends SessionTestBase{
    

    @ClassMapping(ns=TEST.NS)
    public static class Entity {
       
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        private long revision;
        
        @Predicate
        private DateTime created;
        
        @Predicate
        private String text;
        
        public Entity(){}
        
        public Entity(long rev, String t, DateTime c){
            revision = rev;
            text = t;
            created = c;
        }

        public long getRevision() {
            return revision;
        }

        public void setRevision(long revision) {
            this.revision = revision;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public DateTime getCreated() {
            return created;
        }

        public void setCreated(DateTime created) {
            this.created = created;
        }       
                                
    }
    
    private List<DateTime> dateTimes = new ArrayList<DateTime>();

    private Entity var1 = Alias.alias(Entity.class, "var1");
    
    private Entity var2 = Alias.alias(Entity.class, "var2");
    
    @Before
    public void setUp() throws StoreException{
        session = createSession(Entity.class);
        DateTime dateTime = new DateTime();   
        dateTime = dateTime.minus(dateTime.getMillisOfSecond());
        for (Long rev : Arrays.asList(5l, 10l, 15l, 20l, 25l, 30l)){
            Entity entity = new Entity(rev, "text", dateTime.plusMinutes(rev.intValue()));
            dateTimes.add(entity.getCreated());
            session.save(entity);
        }
        session.clear();        
    }
    
    @Test
    public void compareLong() throws StoreException, IOException{                
        Entity result = session.from($(var1))
            .where(
                sub($(var2))
               .where($(var2).ne($(var1)), $(var2.getRevision()).gt($(var1.getRevision())))
               .notExists())
            .uniqueResult($(var1));
        assertNotNull(result);
        assertEquals(30l, result.getRevision());
    }   
    
    @Test
    public void compareDateTime(){
        Entity result = session.from($(var1))
            .where(
                sub($(var2))
               .where($(var2).ne($(var1)), $(var2.getCreated()).gt($(var1.getCreated())))
               .notExists())
            .uniqueResult($(var1));
        assertNotNull(result);
        assertEquals(dateTimes.get(dateTimes.size()-1), result.getCreated());
    }
    
    private BeanSubQuery sub(PEntity<?> entity){
        return new BeanSubQuery().from(entity);
    }
    

}
