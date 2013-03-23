package com.mysema.rdfbean.virtuoso;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.*;

public class GraphQueryTest extends AbstractConnectionTest {

    private static final QNODE<ID> subject = new QNODE<ID>(ID.class, "s");

    private static final QNODE<UID> predicate = new QNODE<UID>(UID.class, "p");

    private static final QNODE<NODE> object = new QNODE<NODE>(NODE.class, "o");

    private RDFQuery query() {
        return new RDFQueryImpl(connection);
    }

    @Test
    public void Pattern_With_Parameters() {
        CloseableIterator<STMT> stmts = query()
                .where(Blocks.SPOC)
                .set(QNODE.s, new UID(TEST.NS))
                .limit(1)
                .construct(Blocks.SPO);
        try {
            while (stmts.hasNext()) {
                System.err.println(stmts.next());
            }
        } finally {
            stmts.close();
        }

    }

    @Test
    public void Patterns() {
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
                .limit(1)
                .construct(Blocks.pattern(subject, predicate, object));
    }

    @Test
    public void Patterns_as_Group() {
        query().where(
                Blocks.group(
                        Blocks.pattern(subject, RDF.type, RDFS.Class),
                        Blocks.pattern(subject, predicate, object)))
                .limit(1)
                .construct(Blocks.pattern(subject, predicate, object));
    }

    @Test
    @Ignore
    public void Two_Patterns() {
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
                .limit(1)
                .construct(
                        Blocks.pattern(subject, RDF.type, RDFS.Class),
                        Blocks.pattern(subject, predicate, object));
    }

    @Test
    @Ignore
    public void Group() {
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
                .limit(1)
                .construct(
                        Blocks.pattern(subject, RDF.type, RDFS.Class),
                        Blocks.pattern(subject, predicate, object));
    }

}
