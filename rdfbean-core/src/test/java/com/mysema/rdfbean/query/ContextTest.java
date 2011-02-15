package com.mysema.rdfbean.query;

import static com.mysema.query.alias.Alias.$;
import static com.mysema.query.alias.Alias.alias;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.domains.ContextDomain;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ContextDomain.Entity1.class, ContextDomain.Entity2.class, ContextDomain.Entity3.class})
public class ContextTest extends SessionTestBase implements ContextDomain{

    private static final Entity1 e1 = alias(Entity1.class);

    private static final Entity2 e2 = alias(Entity2.class);

    private static final Entity3 e3 = alias(Entity3.class);

    @Before
    public void setUp(){
        Entity1 entity1 = new Entity1();
        entity1.property = "X";
        Entity2 entity2 = new Entity2();
        entity2.property = "X";
        Entity3 entity3 = new Entity3();
        entity3.property = "X";

        session.save(entity1);
        session.save(entity2);
        session.save(entity3);
        session.flush();
    }

    @Test
    public void Counts(){
        assertEquals(1, session.from($(e1)).where($(e1.getProperty()).isNotNull()).list($(e1)).size());
        assertEquals(1, session.from($(e2)).where($(e2.getProperty()).isNotNull()).list($(e2)).size());
        assertEquals(1, session.from($(e3)).where($(e3.getProperty()).isNotNull()).list($(e3)).size());
    }

    @Test
    @Ignore
    public void Entity1_property(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e1)).where($(e1.getProperty()).isNotNull());
//        query.list($(e1));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // rdf:type
//        StatementPattern rdf_type = (StatementPattern) join.getArg(0);
//        assertEquals(RDF.type.getId(), rdf_type.getPredicateVar().getValue().stringValue());
//        assertEquals(NS1, rdf_type.getContextVar().getValue().stringValue());
//
//        // property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS1, property.getContextVar().getValue().stringValue());
    }

    @Test
    @Ignore
    public void Entity1_entity(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e1)).where(
//                $(e1.getEntity()).isNotNull(),
//                $(e1.getEntity().getProperty()).isNotNull());
//        query.list($(e1));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // entity
//        StatementPattern entity = (StatementPattern) ((Join) join.getArg(0)).getArg(1);
//        assertEquals(TEST.NS + "entity", entity.getPredicateVar().getValue().stringValue());
//        assertEquals(NS1, entity.getContextVar().getValue().stringValue());
//
//        // entity.property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS2, property.getContextVar().getValue().stringValue());
    }

    @Test
    @Ignore
    public void Entity2_property(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e2)).where($(e2.getProperty()).isNotNull());
//        query.list($(e2));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // rdf:type
//        StatementPattern rdf_type = (StatementPattern) join.getArg(0);
//        assertEquals(RDF.type.getId(), rdf_type.getPredicateVar().getValue().stringValue());
//        assertNull(rdf_type.getContextVar());
//
//        // property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS2, property.getContextVar().getValue().stringValue());

    }

    @Test
    @Ignore
    public void Entity2_entity(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e2)).where(
//                $(e2.getEntity()).isNotNull(),
//                $(e2.getEntity().getProperty()).isNotNull());
//        query.list($(e2));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // entity
//        StatementPattern entity = (StatementPattern) ((Join) join.getArg(0)).getArg(1);
//        assertEquals(TEST.NS + "entity", entity.getPredicateVar().getValue().stringValue());
//        assertNull(entity.getContextVar());
//
//        // entity.property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS3, property.getContextVar().getValue().stringValue());
    }

    @Test
    @Ignore
    public void Entity3_property(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e3)).where($(e3.getProperty()).isNotNull());
//        query.list($(e3));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // rdf:type
//        StatementPattern rdf_type = (StatementPattern) join.getArg(0);
//        assertEquals(RDF.type.getId(), rdf_type.getPredicateVar().getValue().stringValue());
//        assertNull(rdf_type.getContextVar());
//
//        // property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS3, property.getContextVar().getValue().stringValue());
    }

    @Test
    @Ignore
    public void Entity3_entity(){
//        SesameBeanQuery query = (SesameBeanQuery) session.from($(e3)).where(
//                $(e3.getEntity()).isNotNull(),
//                $(e3.getEntity().getProperty()).isNotNull());
//        query.list($(e3));
//        Join join = (Join) query.getJoinBuilder().getTupleExpr();
//
//        // entity
//        StatementPattern entity = (StatementPattern) ((Join) join.getArg(0)).getArg(1);
//        assertEquals(TEST.NS + "entity", entity.getPredicateVar().getValue().stringValue());
//        assertNull(entity.getContextVar());
//
//        // entity.property
//        StatementPattern property = (StatementPattern) join.getArg(1);
//        assertEquals(TEST.NS + "property", property.getPredicateVar().getValue().stringValue());
//        assertEquals(NS1, property.getContextVar().getValue().stringValue());
    }

}
