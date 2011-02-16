package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.TEST;

public class TupleQueryTest {

    private static final QNODE<ID> subject = new QNODE<ID>(ID.class, "s");

    private static final QNODE<UID> predicate = new QNODE<UID>(UID.class, "p");

    private static final QNODE<NODE> object = new QNODE<NODE>(NODE.class, "o");

    private final MiniConnection connection = new MiniConnection(new MiniRepository());

    private RDFQuery query(){
        return new RDFQueryImpl(connection);
    }

    @Test
    public void Pattern(){
        query().where(Blocks.pattern(subject, RDF.type, RDFS.Class)).select(subject);
    }

    @Test
    public void Pattern_with_Filters(){
        Block pattern = Blocks.pattern(subject, predicate, object);

        List<Predicate> filters = Arrays.<Predicate>asList(
                subject.eq(new UID(TEST.NS)),
                predicate.eq(RDFS.label),
                subject.ne(new UID(TEST.NS)),
                object.isNull(),
                object.isNotNull(),
                object.lit().lt("X"),
                object.lit().gt("X"),
                object.lit().loe("X"),
                object.lit().goe("X")
        );

        for (Predicate filter : filters){
            query().where(pattern, filter).select(subject);
        }
    }

    @Test
    public void Pattern_with_Limit_and_Offset(){
        query().where(Blocks.pattern(subject, RDF.type, RDFS.Class))
                .limit(5)
                .offset(20)
                .select(subject);
    }

    @Test
    public void Group(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
                .select(subject, predicate, object);
    }

    @Test
    public void Union(){
        query().where(
                Blocks.union(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)
                )).select(subject, predicate, object);
    }

    @Test
    public void Optional(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.optional(Blocks.pattern(subject, predicate, object)))
                .select(subject, predicate, object);
    }

    @Test
    @Ignore
    public void Complex(){
        QID u = new QID("u"), u2 = new QID("u2");
        QLIT label = new QLIT("label");
        UID User = new UID(TEST.NS, "User");

        ID id = new BID(), id2 = new BID(), id3 = new BID();
        connection.addStatements(
                new STMT(id, RDF.type, User),
                new STMT(id2, RDF.type, User),
                new STMT(id3, RDF.type, User),
                new STMT(id, RDFS.label, new LIT("x")),
                new STMT(id2, RDFS.label, new LIT("x")),
                new STMT(id3, RDFS.label, new LIT("y")));

        CloseableIterator<Map<String, NODE>> iterator =
            query().where(
                Blocks.pattern(u,  RDF.type, User),
                Blocks.pattern(u2, RDF.type, User),
                Blocks.pattern(u2, RDFS.label, label),
                Blocks.pattern(u,  RDFS.label, label),
                u.ne(u2)
                ).select(u, u2);

        assertTrue(iterator.hasNext());
        while (iterator.hasNext()){
            assertFalse(iterator.next().isEmpty());
        }
    }

}
