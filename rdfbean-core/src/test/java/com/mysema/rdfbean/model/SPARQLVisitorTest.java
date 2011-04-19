package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Ops;
import com.mysema.query.types.PredicateOperation;

@SuppressWarnings("unchecked")
public class SPARQLVisitorTest {
    
    private final SPARQLVisitor visitor = new SPARQLVisitor();
        
    @Test
    public void Like_as_Matches(){     
        visitor.setLikeAsMatches(true);
        visitor.handle(new PredicateOperation(Ops.LIKE, QNODE.o, new ConstantImpl(LIT.class, new LIT("x%"))));
        assertEquals("regex(?o, ?_c2)", visitor.toString());
        assertTrue(visitor.getConstantToLabel().containsKey(new LIT("x.*")));
    }
    
    @Test
    public void Like_as_Matches_2(){
        visitor.setLikeAsMatches(true);
        visitor.handle(new PredicateOperation(Ops.LIKE, QNODE.o, new ConstantImpl(LIT.class, new LIT("x_"))));
        assertEquals("regex(?o, ?_c2)", visitor.toString());
        assertTrue(visitor.getConstantToLabel().containsKey(new LIT("x.")));
    }

}
