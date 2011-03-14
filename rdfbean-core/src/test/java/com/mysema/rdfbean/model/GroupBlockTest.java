package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class GroupBlockTest {

    @Test
    public void To_String(){
        Block block = Blocks.group(Blocks.S_FIRST, Blocks.S_REST);
        assertEquals(
            "{ {s} http://www.w3.org/1999/02/22-rdf-syntax-ns#first {first} . " +
            "{s} http://www.w3.org/1999/02/22-rdf-syntax-ns#rest {rest} .  }",
            block.toString());
    }

    @Test
    public void Exists(){
        assertEquals("exists { {s} {p} {o} .  }", Blocks.group(Blocks.SPO).exists().toString());
    }

    @Test
    public void Equals(){
        Block block1 = Blocks.group(Blocks.S_FIRST, Blocks.S_REST);
        Block block2 = Blocks.group(Blocks.S_FIRST);
        assertFalse(block1.equals(block2));
    }

}
