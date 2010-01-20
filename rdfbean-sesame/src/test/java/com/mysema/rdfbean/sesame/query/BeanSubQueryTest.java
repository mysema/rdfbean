package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.object.Session;
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
        @Predicate
        private long revision;
        
        @Predicate
        private String text;
        
        public Entity(){}
        
        public Entity(long rev, String t){
            revision = rev;
            text = t;
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
                                
    }
    
    private Session session;
    
    @After
    public void tearDown() throws IOException{
        if (session != null) session.close();
    }
    
    @Test
    public void test() throws StoreException, IOException{
        Session session = createSession(Entity.class);
        for (Long rev : Arrays.asList(5l, 10l, 15l, 20l, 25l, 30l)){
            session.save(new Entity(rev, "text"));
        }
        session.clear();
        
        Entity var1 = Alias.alias(Entity.class, "var1");
        Entity var2 = Alias.alias(Entity.class, "var2");
        
        Entity result = session.from($(var1))
            .where(
                sub($(var2))
               .where($(var2).ne($(var1)), $(var2.getRevision()).gt($(var1.getRevision())))
               .notExists())
            .uniqueResult($(var1));
        assertNotNull(result);
        assertEquals(30l, result.getRevision());
    }   
    
    private BeanSubQuery sub(PEntity<?> entity){
        return new BeanSubQuery().from(entity);
    }
    

}
