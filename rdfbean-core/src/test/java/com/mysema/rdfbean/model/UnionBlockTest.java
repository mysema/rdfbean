package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnionBlockTest {

    @Test
    public void To_String() {
        Block block = Blocks.union(Blocks.S_FIRST, Blocks.S_REST);
        assertEquals(
                "{s} http://www.w3.org/1999/02/22-rdf-syntax-ns#first {first} . " +
                        "UNION " +
                        "{s} http://www.w3.org/1999/02/22-rdf-syntax-ns#rest {rest} .",
                block.toString());
    }

    @Test
    public void Exists() {
        Block block = Blocks.union(Blocks.S_FIRST, Blocks.S_REST);
        assertEquals("exists {s} http://www.w3.org/1999/02/22-rdf-syntax-ns#first {first} . " +
                "UNION {s} http://www.w3.org/1999/02/22-rdf-syntax-ns#rest {rest} .",
                block.exists().toString());
    }

}
