/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean;

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

    private static Map<String, String> ns2prefix = new HashMap<String, String>();
    
    public static void register(String prefix, String ns){        
        ns2prefix.put(ns, prefix);
    }
    
    public static String getPrefix(String ns) {
        return ns2prefix.get(ns);
    }

    static{
        ns2prefix.put(SRV.NS,  "srv");
        ns2prefix.put(RDF.NS,  "rdf");
        ns2prefix.put(RDFS.NS, "rdfs");
        ns2prefix.put(TEST.NS, "test");
        ns2prefix.put(OWL.NS,  "owl");
        ns2prefix.put(XSD.NS,  "xsd");        
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

}
