package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * NodeConverterTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NodeConverterTest {
    
    private final NodeConverter converter = NodeConverter.DEFAULT;
    
    @Test
    public void test(){
        List<NODE> nodes = Arrays.<NODE>asList(
                new LIT("lit"),
                new LIT("lit", "fi"),
                new LIT("lit", "f.i"),
//                new LIT("lit", "f|i"),
                new LIT("lit", XSD.stringType),
                new LIT("lit", new UID("http://www.test.com")),
//                new LIT("lit", new UID("http://www.test|com")),
                new UID("http://www.test.com"),
                XSD.stringType,
                RDF.Property,
                new BID("nodeA"),
                new BID()
        );
        
        for (NODE node : nodes){
            String str = converter.toString(node);            
            System.out.println(str);
            assertEquals(node, converter.fromString(str));
        }
    }

}
