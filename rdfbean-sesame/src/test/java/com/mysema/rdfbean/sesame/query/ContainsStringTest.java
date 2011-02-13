/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Entity;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.EntityRevision;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Term;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Entity.class, EntityRevision.class, Term.class})
public class ContainsStringTest extends SessionTestBase implements EntityRevisionTermDomain{

    @Test
    public void test(){
        EntityRevision revision = new EntityRevision();
        revision.text = "a bcd e";
        session.save(revision);
        session.flush();
        session.clear();

        EntityRevision var = Alias.alias(EntityRevision.class);
        assertNotNull(session.from($(var)).where($(var.getText()).containsIgnoreCase("a bcd e")).uniqueResult($(var)));
    }

    @Test
    public void test2(){
        // 1
        Entity entity = new Entity();
        session.save(entity);

        EntityRevision rev = new EntityRevision();
        rev.text = "def";
        rev.revisionOf = entity;
        session.save(rev);
        entity.latestRevision = rev;
        session.save(entity);

        // 2

        Term term = new Term();
        term.text2 = "abc";
        session.save(term);

        Entity entity2 = new Entity();
        entity2.term = term;
        session.save(entity2);

        EntityRevision rev2 = new EntityRevision();
        rev2.revisionOf = entity2;
        session.save(rev2);
        entity2.latestRevision = rev2;
        session.save(entity2);

        // query 1
        EntityRevision var = Alias.alias(EntityRevision.class);
        BooleanBuilder builder = new BooleanBuilder();
        builder.or($(var.getText()).containsIgnoreCase("abc"));
        builder.or($(var.getRevisionOf().getTerm().getText2()).containsIgnoreCase("abc"));
        builder.and($(var).eq($(var.getRevisionOf().getLatestRevision())));
        assertEquals(1, session.from($(var)).where(builder.getValue()).list($(var)).size());

        // query 2
        builder = new BooleanBuilder();
        builder.or($(var.getText()).containsIgnoreCase("def"));
        builder.or($(var.getRevisionOf().getTerm().getText2()).containsIgnoreCase("def"));
        builder.and($(var).eq($(var.getRevisionOf().getLatestRevision())));
        assertEquals(1, session.from($(var)).where(builder.getValue()).list($(var)).size());
    }

}
