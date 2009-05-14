/**
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=RDF.NS, ln="List")
public class RDFList {
    
    @Id(IDType.RESOURCE)
    private ID id;
    
    @Predicate
    private Object first;

    @Predicate
    private RDFList rest;

    public Object getFirst() {
        return first;
    }

    public void setFirst(Object first) {
        this.first = first;
    }

    public RDFList getRest() {
        return rest;
    }

    public void setRest(RDFList rest) {
        this.rest = rest;
    }
    
    public boolean isNil() {
        return RDF.nil.equals(id);
    }
    
}
