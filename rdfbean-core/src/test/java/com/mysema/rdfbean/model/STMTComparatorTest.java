package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.mysema.rdfbean.TEST;

public class STMTComparatorTest {
    
    private STMTComparator comparator = STMTComparator.DEFAULT;
    
    @Test
    public void test(){
        BID bid = new BID("c");
        UID uid = new UID("b:b");
        LIT lit = new LIT("a");
        
        STMT stmt1 = new STMT(bid, uid, bid);
        STMT stmt2 = new STMT(bid, uid, uid);
        STMT stmt3 = new STMT(bid, uid, lit);
        STMT stmt4 = new STMT(uid, uid, bid);
        STMT stmt5 = new STMT(uid, uid, uid);
        STMT stmt6 = new STMT(uid, uid, lit);
        List<STMT> stmts = Arrays.<STMT>asList(stmt6, stmt5, stmt4, stmt3, stmt2, stmt1);
        Collections.sort(stmts, comparator);
        assertEquals(Arrays.asList(stmt1, stmt2, stmt3, stmt4, stmt5, stmt6), stmts); 
    }

    @Test
    public void test2(){        
        STMT stmt1 = new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("a"));
        STMT stmt2 = new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("b"));
        STMT stmt3 = new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("c"));
        STMT stmt4 = new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("d"));
        List<STMT> stmts = Arrays.<STMT>asList(stmt4, stmt3, stmt2, stmt1);
        Collections.sort(stmts, comparator);
        assertEquals(Arrays.asList(stmt1, stmt2, stmt3, stmt4), stmts);
        
    }
}
