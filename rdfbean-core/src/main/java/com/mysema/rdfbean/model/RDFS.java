/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collection;


/**
 * Namespace file for the RDF Schema namespace
 * 
 * @author sasa
 *
 */
public final class RDFS {
    public static final String NS = "http://www.w3.org/2000/01/rdf-schema#";

    public static final UID Resource = new UID(NS, "Resource");

    public static final UID Literal = new UID(NS, "Literal");

    public static final UID Class = new UID(NS, "Class");

    public static final UID subClassOf = new UID(NS, "subClassOf");

    public static final UID subPropertyOf = new UID(NS, "subPropertyOf");

    public static final UID domain = new UID(NS, "domain");

    public static final UID range = new UID(NS, "range");

    public static final UID comment = new UID(NS, "comment");

    public static final UID label = new UID(NS, "label");

    public static final UID Datatype = new UID(NS, "Datatype");

    public static final UID Container = new UID(NS, "Container");

    public static final UID member = new UID(NS, "member");

    public static final UID isDefinedBy = new UID(NS, "isDefinedBy");

    public static final UID seeAlso = new UID(NS, "seeAlso");

    public static final UID ContainerMembershipProperty = new UID(NS,
            "ContainerMembershipProperty");

    public static final Collection<UID> ALL = Arrays.asList(
            Resource,
            Literal,
            Class,
            subClassOf,
            subPropertyOf,
            domain,
            range,
            comment,
            label,
            Datatype,
            Container,
            member,
            isDefinedBy,
            seeAlso,
            ContainerMembershipProperty);
    
    private RDFS() {}
}
