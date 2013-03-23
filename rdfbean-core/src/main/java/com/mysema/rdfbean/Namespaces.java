/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;

/**
 * Namespaces provides utility methods for namespace to prefix mappings
 * 
 * @author tiwe
 * @version $Id$
 * 
 */
public final class Namespaces {

    public static final Map<String, String> DEFAULT;

    static {
        Map<String, String> defaultMappings = new HashMap<String, String>();
        defaultMappings.put(RDF.NS, "rdf");
        defaultMappings.put(RDFS.NS, "rdfs");
        defaultMappings.put(OWL.NS, "owl");
        defaultMappings.put(XSD.NS, "xsd");
        DEFAULT = Collections.unmodifiableMap(defaultMappings);
    }

    private Namespaces() {
    }

}
