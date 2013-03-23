package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

public class NODEComparatorTest {

    private static final NODEComparator comparator = new NODEComparator();

    @Test
    public void Compare() {
        BID bid = new BID("c");
        UID uid = new UID("b:b");
        LIT lit = new LIT("a");
        assertEquals(0, comparator.compare(bid, bid));
        assertEquals(0, comparator.compare(uid, uid));
        assertEquals(0, comparator.compare(lit, lit));
    }

    @Test
    public void NodeType() {
        BID bid = new BID("c");
        UID uid = new UID("b:b");
        LIT lit = new LIT("a");
        List<NODE> nodes = Arrays.<NODE> asList(lit, uid, bid);
        Collections.sort(nodes, comparator);
        assertEquals(Arrays.asList(bid, uid, lit), nodes);
    }

    @Test
    public void Lexical() {
        LIT lit1 = new LIT("x");
        LIT lit2 = new LIT("y");
        LIT lit3 = new LIT("z");
        List<NODE> nodes = Arrays.<NODE> asList(lit3, lit2, lit1);
        Collections.sort(nodes, comparator);
        assertEquals(Arrays.asList(lit1, lit2, lit3), nodes);
    }

    @Test
    public void Literals() {
        LIT lit1 = new LIT("x", RDF.text);
        LIT lit2 = new LIT("x", new Locale("fi"));
        LIT lit3 = new LIT("x");
        List<NODE> nodes = Arrays.<NODE> asList(lit3, lit2, lit1);
        Collections.sort(nodes, comparator);
        assertEquals(Arrays.asList(lit1, lit2, lit3), nodes);
    }
}
