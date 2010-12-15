package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;


public class NTriplesUtilTest {

    @SuppressWarnings("unchecked")
    @Test
    public void UID_serialization(){
        for (Collection<UID> schema : Arrays.asList(XSD.ALL, RDF.ALL, RDFS.ALL, OWL.ALL)){
            for (UID uid : schema){
                assertEquals("<"+ uid.getId() + ">", NTriplesUtil.toString(uid));
            }
        }
    }
    
    @Test
    public void BID_serialization(){
        for (int i = 0; i < 100; i++){
            BID bid = new BID();
            assertEquals("_:b" + bid.getId(), NTriplesUtil.toString(bid)); 
        }
    }
    
    @Test
    public void LIT_serialization(){
        assertEquals("\"X\"", NTriplesUtil.toString(new LIT("X")));
        assertEquals("\"X\"@en", NTriplesUtil.toString(new LIT("X", Locale.ENGLISH)));
    }
    
}
