/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;

/**
 * RDFData provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFTestData {    

    public static final List<LIT> literals = Arrays.asList(
            new LIT("test"),
            new LIT("test", "en"),
            new LIT("test2", "en"),
            new LIT("test3", "fi"),
            new LIT("test4", new UID(TEST.NS, "custType")),
            new LIT("test5", new UID("http://mycompany.com/", "test"))
    );
    
    public static final List<NODE> objects = new ArrayList<NODE>();
    
    public static final List<UID> predicates = Arrays.asList(
            OWL.allValuesFrom,
            RDF.first,
            RDFS.comment,
            RDF.type,
            new UID(TEST.NS, "friend")            
    );
    
    public static final List<UID> subjects = Arrays.asList(
            XSD.stringType,
            OWL.Class,
            RDF.Alt,
            RDFS.comment,
            new UID(TEST.NS, "bob"),
            new UID(TEST.NS, "anne"),
            new UID(TEST.NS, "Person"),
            new UID("http://mycompany.com/", "test")            
    );
    
    static{
        objects.addAll(subjects);
        objects.addAll(literals);
    }

}
