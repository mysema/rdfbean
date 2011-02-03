package com.mysema.rdfbean.model;

import org.junit.Test;

public class BooleanQueryTest {
    
    private static final QNODE<ID> subject = new QNODE<ID>(ID.class, "s");
    
    private static final QNODE<UID> predicate = new QNODE<UID>(UID.class, "p");
    
    private static final QNODE<NODE> object = new QNODE<NODE>(NODE.class, "o");
    
    private RDFQuery query(){
        return new RDFTestQuery();
    }    
    
    @Test
    public void Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .ask();
    }
    
    @Test
    public void Patterns_as_Group(){
        query().where(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)))
               .ask();
    }

}
