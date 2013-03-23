package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class GraphBlockTest {

    @Test
    public void To_String() {
        Block block = Blocks.graph(new QNODE<UID>(UID.class, "c"), Blocks.SPO);
        assertEquals("GRAPH {c}{ {s} {p} {o} .  }", block.toString());
    }

    @Test
    public void To_String_with_Filter() {
        Block block = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPO, QNODE.o.isNull());
        assertEquals("GRAPH {c}{ {s} {p} {o} .  FILTER({o} is null) }", block.toString());
    }

    @Test
    public void Exists() {
        Block block = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPO);
        assertEquals("exists GRAPH {c}{ {s} {p} {o} .  }", block.exists().toString());
    }

    @Test
    public void Equals() {
        Block block1 = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPO);
        Block block2 = Blocks.graphFilter(new QNODE<UID>(UID.class, "c"), Blocks.SPOC);
        Block block3 = Blocks.graphFilter(new QNODE<UID>(UID.class, "d"), Blocks.SPO);
        assertFalse(block1.equals(block2));
        assertFalse(block1.equals(block3));
        assertFalse(block2.equals(block3));
    }
}
