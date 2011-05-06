package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryFlag.Position;
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

    @Test
    public void Start_Flag() {
        final String PREFIX = "DEFINE input:inference \"ruleset\" ";
        RDFQueryImpl query = new RDFQueryImpl(null);
        query.addFlag(Position.START, PREFIX);
        query.where(Blocks.SPO);
        query.aggregateFilters();
        QueryMetadata metadata = query.getMetadata();
        metadata.addProjection(QNODE.s);
        visitor.visit(metadata, QueryLanguage.TUPLE);
        assertEquals(stripWS(PREFIX + "SELECT ?s WHERE { ?s ?p ?o }"), stripWS(visitor.toString()));
    }
    
    public static String stripWS(String str) {
        return str.replaceAll("\\s", "");
    }
}
