/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.Arrays;
import java.util.Collection;

import com.mysema.rdfbean.model.UID;

/**
 * OWL defines concepts from the OWL ontology
 * 
 * @author sasa
 * 
 */
public final class OWL {

    public static final String NS = "http://www.w3.org/2002/07/owl#";

    public static final UID Class = new UID(NS, "Class");

    public static final UID Individual = new UID(NS, "Individual");

    public static final UID equivalentClass = new UID(NS, "equivalentClass");

    public static final UID equivalentProperty = new UID(NS, "equivalentProperty");

    public static final UID sameAs = new UID(NS, "sameAs");

    public static final UID differentFrom = new UID(NS, "differentFrom");

    public static final UID AllDifferent = new UID(NS, "AllDifferent");

    public static final UID distinctMembers = new UID(NS, "distinctMembers");

    public static final UID ObjectProperty = new UID(NS, "ObjectProperty");

    public static final UID DatatypeProperty = new UID(NS, "DatatypeProperty");

    public static final UID inverseOf = new UID(NS, "inverseOf");

    public static final UID TransitiveProperty = new UID(NS, "TransitiveProperty");

    public static final UID SymmetricProperty = new UID(NS, "SymmetricProperty");

    public static final UID FunctionalProperty = new UID(NS, "FunctionalProperty");

    public static final UID InverseFunctionalProperty = new UID(NS, "InverseFunctionalProperty");

    public static final UID Restriction = new UID(NS, "Restriction");

    public static final UID onProperty = new UID(NS, "onProperty");

    public static final UID allValuesFrom = new UID(NS, "allValuesFrom");

    public static final UID someValuesFrom = new UID(NS, "someValuesFrom");

    public static final UID minCardinality = new UID(NS, "minCardinality");

    public static final UID maxCardinality = new UID(NS, "maxCardinality");

    public static final UID cardinality = new UID(NS, "cardinality");

    public static final UID Ontology = new UID(NS, "Ontology");

    public static final UID imports = new UID(NS, "imports");

    public static final UID intersectionOf = new UID(NS, "intersectionOf");

    public static final UID versionInfo = new UID(NS, "versionInfo");

    public static final UID priorVersion = new UID(NS, "priorVersion");

    public static final UID backwardCompatibleWith = new UID(NS, "backwardCompatibleWith");

    public static final UID incompatibleWith = new UID(NS, "incompatibleWith");

    public static final UID DeprecatedClass = new UID(NS, "DeprecatedClass");

    public static final UID DeprecatedProperty = new UID(NS, "DeprecatedProperty");

    public static final UID AnnotationProperty = new UID(NS, "AnnotationProperty");

    public static final UID OntologyProperty = new UID(NS, "OntologyProperty");

    public static final UID oneOf = new UID(NS, "oneOf");

    public static final UID hasValue = new UID(NS, "hasValue");

    public static final UID disjointWith = new UID(NS, "disjointWith");

    public static final UID unionOf = new UID(NS, "unionOf");

    public static final UID complementOf = new UID(NS, "complementOf");
    
    public static final UID Thing = new UID(NS, "Thing");
    
    public static final Collection<UID> ALL = Arrays.asList(
            AllDifferent,
            allValuesFrom,
            AnnotationProperty,
            backwardCompatibleWith,
            cardinality,
            Class,
            complementOf,
            DatatypeProperty,
            DeprecatedClass,
            DeprecatedProperty,
            differentFrom,
            disjointWith,
            distinctMembers,
            equivalentClass,
            equivalentProperty,
            FunctionalProperty,
            hasValue,
            imports,
            incompatibleWith,
            Individual,
            intersectionOf,
            InverseFunctionalProperty,
            inverseOf,
            maxCardinality,
            minCardinality,
            ObjectProperty,
            oneOf,
            onProperty,
            Ontology,
            OntologyProperty,
            priorVersion,
            Restriction,
            sameAs,
            someValuesFrom,
            SymmetricProperty,
            Thing,
            TransitiveProperty
    );

    private OWL() {
    }
}
