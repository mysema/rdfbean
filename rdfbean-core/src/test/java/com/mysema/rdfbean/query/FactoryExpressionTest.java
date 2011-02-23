package com.mysema.rdfbean.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.types.Expression;
import com.mysema.query.types.QBean;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({SimpleType.class, SimpleType2.class})
public class FactoryExpressionTest extends SessionTestBase implements SimpleDomain{
    
    private PathBuilder<SimpleType> var = new PathBuilder<SimpleType>(SimpleType.class, "var");
    
    private Expression<SimpleType> projection = new QBean<SimpleType>(SimpleType.class, var.getString("directProperty"));
    
    @Before
    public void setUp(){
        session.saveAll(new SimpleType("a"), new SimpleType("b"), new SimpleType("c"), new SimpleType("d"));
    }
    
    @Test
    public void List(){
        List<SimpleType> result = session.from(var).list(projection);
        assertEquals(4, result.size());
        for (SimpleType st : result){
            assertNotNull(st.directProperty);
        }
    }
    
    @Test
    public void UniqueResult(){
        assertTrue(session.from(var).limit(1).uniqueResult(projection) instanceof SimpleType);
    }
    
    @Test
    public void Iterate(){
        CloseableIterator<SimpleType> it = session.from(var).iterate(projection);
        try{
            assertTrue(it.next() instanceof SimpleType);
            assertTrue(it.next() instanceof SimpleType);
            assertTrue(it.next() instanceof SimpleType);
            assertTrue(it.next() instanceof SimpleType);
            assertFalse(it.hasNext());
        }finally{
            it.close();
        }
        
    }

}
