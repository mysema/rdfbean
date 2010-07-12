package com.mysema.rdfbean.rdb.query;

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

import com.mysema.query.alias.Alias;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.domains.EntityDomain.Entity;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.TestConfig;

@TestConfig(Entity.class)
public class BeanSubQueryTest extends AbstractRDBTest implements EntityDomain{
    
    private List<DateTime> dateTimes = new ArrayList<DateTime>();

    private Entity var1 = Alias.alias(Entity.class, "var1");
    
    private Entity var2 = Alias.alias(Entity.class, "var2");
    
    @Before
    public void setUp(){
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
    public void compareLong() throws  IOException{                
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
