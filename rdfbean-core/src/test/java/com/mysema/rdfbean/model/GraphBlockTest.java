package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class GraphBlockTest {
    
    @Test
    public void To_String(){
        Block block = Blocks.graph(new QNODE<UID>(UID.class, "c"), Blocks.SPO);
        assertEquals("GRAPH {c}{ {s} {p} {o} .  }", block.toString());
    }

    @Test
    public void To_String_with_Filter(){
        Block block = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPO, QNODE.o.isNull());
        assertEquals("GRAPH {c}{ {s} {p} {o} .  FILTER({o} is null) }", block.toString());
    }

    @Test
    public void Exists(){
        Block block = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPO);
        assertEquals("exists GRAPH {c}{ {s} {p} {o} .  }", block.exists().toString());
    }
    
}
