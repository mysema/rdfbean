package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.Nodes;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


public class NTriplesUtilTest {

    @Test
    public void UID_serialization(){
        for (UID uid : Nodes.all){
            assertEquals("<"+ uid.getId() + ">", NTriplesWriter.toString(uid));
        }
    }

    @Test
    public void BID_serialization(){
        for (int i = 0; i < 100; i++){
            BID bid = new BID();
            assertEquals("_:" + bid.getId(), NTriplesWriter.toString(bid));
        }
    }

    @Test
    public void LIT_serialization(){
        assertEquals("\"X\"^^<"+XSD.stringType.getId()+">", NTriplesWriter.toString(new LIT("X")));
        assertEquals("\"X\"@en", NTriplesWriter.toString(new LIT("X", Locale.ENGLISH)));
    }

}
