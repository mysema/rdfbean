/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collection;



/**
 * Namespace file for the RDF namespace
 * 
 * @author sasa
 *
 */
public final class RDF {
    public static final String NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final UID type = new UID(NS, "type");

    public static final UID Property = new UID(NS, "Property");

    public static final UID XMLLiteral = new UID(NS, "XMLLiteral");

    public static final UID subject = new UID(NS, "subject");

    public static final UID predicate = new UID(NS, "predicate");

    public static final UID object = new UID(NS, "object");

    public static final UID Statement = new UID(NS, "Statement");

    public static final UID Bag = new UID(NS, "Bag");

    public static final UID Alt = new UID(NS, "Alt");

    public static final UID Seq = new UID(NS, "Seq");

    public static final UID value = new UID(NS, "value");

    public static final UID li = new UID(NS, "li");

    public static final UID List = new UID(NS, "List");

    public static final UID first = new UID(NS, "first");

    public static final UID rest = new UID(NS, "rest");

    public static final UID nil = new UID(NS, "nil");

    public static final UID text = new UID(NS, "text");
    
    public static final Collection<UID> ALL = Arrays.asList(
            type,
            Property,
            XMLLiteral,
            subject,
            predicate,
            object,
            Statement,
            Bag,
            Alt,
            Seq,
            value,
            li,
            List,
            first,
            rest,
            nil,
            text);
    
    
    private RDF() {}

    public static UID getContainerMembershipProperty(int i) {
        if (i < 1) {
            throw new IllegalArgumentException("Negative index: " + i);
        }
        return new UID(NS, "_"+i);
    }

    public static boolean isContainerMembershipProperty(UID predicate) {
        return NS.equals(predicate.ns()) && isContainerMembershipPropertyLocalName(predicate.ln());
    }
    
    public static boolean isContainerMembershipPropertyLocalName(String ln) {
        boolean result = false;
        if (ln.length() >= 2 && ln.charAt(0) == '_') {
            char c = ln.charAt(1);
            if ('0' < c && c <= '9') {
                result = true;
                for (int i=2; i < ln.length() && result; i++) {
                    c = ln.charAt(i);
                    result = '0' <= c && c <= '9';
                }
            }
        }
        return result;
    }

}
