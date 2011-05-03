package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QIDTest {

    QID subject = new QID("subject");
    QID context = new QID("context");
    QNODE<UID> predicate = new QNODE<UID>(UID.class, "predicate");
    QNODE<NODE> object = new QNODE<NODE>(NODE.class, "object");


    @Test
    public void Pattern_via_has(){
        assertEquals(
                Blocks.pattern(subject, predicate, object),
                subject.has(predicate, object));
    }
    
    @Test
    public void Pattern_via_has_with_Context(){
        assertEquals(
                Blocks.pattern(subject, predicate, object, context),
                subject.has(predicate, object, context));
    }

    @Test
    public void Optional_via_has(){
        assertEquals(
                Blocks.optional(Blocks.pattern(subject, predicate, object)),
                subject.has(predicate, object).asOptional());
    }

    @Test
    public void Pattern_via_a(){
        assertEquals(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.a(RDFS.Class));
    }
    
    @Test
    public void Pattern_via_a_with_Context(){
        assertEquals(
                Blocks.pattern(subject, RDF.type, RDFS.Class, context),
                subject.a(RDFS.Class, context));
    }

    @Test
    public void Pattern_via_is(){
        assertEquals(
                Blocks.pattern(object, predicate, subject),
                subject.is(predicate, object));
    }

}
