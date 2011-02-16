package com.mysema.rdfbean.model;

import org.junit.Ignore;
import org.junit.Test;

public class GraphQueryTest {

    private static final QNODE<ID> subject = new QNODE<ID>(ID.class, "s");

    private static final QNODE<UID> predicate = new QNODE<UID>(UID.class, "p");

    private static final QNODE<NODE> object = new QNODE<NODE>(NODE.class, "o");

    private final MiniConnection connection = new MiniConnection(new MiniRepository());

    private RDFQuery query(){
        return new RDFQueryImpl(connection);
    }

    @Test
    public void Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(Blocks.pattern(subject, predicate, object));
    }

    @Test
    public void Patterns_as_Group(){
        query().where(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)))
               .construct(Blocks.pattern(subject, predicate, object));
    }

    @Test
    @Ignore
    public void Two_Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(
                   Blocks.pattern(subject, RDF.type,  RDFS.Class),
                   Blocks.pattern(subject, predicate, object));
    }

    @Test
    @Ignore
    public void Group(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(
                   Blocks.pattern(subject, RDF.type,  RDFS.Class),
                   Blocks.pattern(subject, predicate, object));
    }


}
