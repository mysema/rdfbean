/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

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
    
    public static final Map<String,String> DEFAULT;
    
    private static Map<String, String> ns2prefix = new HashMap<String, String>();
    
    static{
        Map<String,String> defaultMappings = new HashMap<String,String>();
        defaultMappings.put(RDF.NS, "rdf");
        defaultMappings.put(RDFS.NS, "rdfs");
        defaultMappings.put(OWL.NS, "owl");
        defaultMappings.put(XSD.NS, "xsd");
        DEFAULT = Collections.unmodifiableMap(defaultMappings);
        
        ns2prefix.putAll(defaultMappings);
        ns2prefix.put(SRV.NS,  "srv");
        ns2prefix.put(TEST.NS, "test");
    }
    
    
    public static void register(String prefix, String ns){        
        ns2prefix.put(ns, prefix);
    }
    
    public static String getPrefix(String ns) {
        return ns2prefix.get(ns);
    }
    
    public static String getReadableURI(String ns, @Nullable String ln) {
        if (ln == null) {
            ln = "";
        }
        if (StringUtils.isNotEmpty(ns)) {
            String prefix = ns2prefix.get(ns);
            if (prefix != null) {
                return prefix+":"+ln;
            } else {
                return "<"+ns+ln+">";
            }
        } else {
            return ln;
        }
    }
    
    private Namespaces(){}


}
