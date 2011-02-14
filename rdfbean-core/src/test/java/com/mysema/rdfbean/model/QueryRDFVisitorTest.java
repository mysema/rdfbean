package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;

public class QueryRDFVisitorTest {

    private final MiniConnection connection = new MiniConnection(new MiniRepository());

    private ID id = new BID();

    private QueryRDFVisitor visitor = new QueryRDFVisitor(connection);
    
    private Bindings context = new Bindings();
    
    @Before
    public void setUp(){        
        connection.addStatements(
                new STMT(id, RDF.type, RDFS.Resource),
                new STMT(id, RDFS.label, new LIT("id")));
    }
    
    @Test
    public void PatternBlock(){
        Iterable<Bindings> iterable = visitor.visit((PatternBlock)Blocks.SPO, context);
        List<Bindings> rows = IteratorAdapter.asList(iterable.iterator());
        assertEquals(2, rows.size());
        assertEquals(3, rows.get(0).toMap().size());
        assertEquals(3, rows.get(1).toMap().size());
        assertEquals(id, rows.get(0).get("s"));
        assertEquals(id, rows.get(1).get("s"));
    }
    
    @Test
    public void PatternBlock_with_unmatching_Filter(){
        GroupBlock block = (GroupBlock) Blocks.filter(Blocks.SPO, QNODE.p.eq(RDF.predicate));
        Iterable<Bindings> iterable = visitor.visit(block, context);
        List<Bindings> rows = IteratorAdapter.asList(iterable.iterator());
        assertEquals(0, rows.size());        
    }
    
    
    @Test
    public void PatternBlock_with_Filter(){
        GroupBlock block = (GroupBlock) Blocks.filter(Blocks.SPO, QNODE.p.eq(RDF.type));
        Iterator<Bindings> iterator = visitor.visit(block, context).iterator();
        Map<String, NODE> row = iterator.next().toMap();
        assertFalse(iterator.hasNext());
        assertEquals(3, row.size());
        assertEquals(id, row.get("s"));
        assertEquals(RDF.type, row.get("p"));
    }
    
}
