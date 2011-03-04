package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QIDTest {

    QID subject = new QID("subject");
    QNODE<UID> predicate = new QNODE<UID>(UID.class, "predicate");
    QNODE<NODE> object = new QNODE<NODE>(NODE.class, "object");


    @Test
    public void Pattern_via_has(){
        assertEquals(
                Blocks.pattern(subject, predicate, object),
                subject.has(predicate, object));
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
    public void Pattern_via_is(){
        assertEquals(
                Blocks.pattern(object, predicate, subject),
                subject.is(predicate, object));
    }

}
