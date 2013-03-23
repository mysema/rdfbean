package com.mysema.rdfbean.rdb.support;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.JoinType;
import com.mysema.query.QueryMetadata;
import com.mysema.rdfbean.rdb.QStatement;
import com.mysema.rdfbean.rdb.QSymbol;

public class SortableQueryMetadataTest {

    @Test
    public void Statements() {
        QueryMetadata metadata = new SortableQueryMetadata();
        QStatement user = new QStatement("user");
        QStatement user_type = new QStatement("user_type");
        QStatement dep = new QStatement("dep");
        QStatement dep_type = new QStatement("dep_type");
        metadata.addJoin(JoinType.DEFAULT, user);
        metadata.addJoin(JoinType.DEFAULT, dep);
        metadata.addJoin(JoinType.INNERJOIN, user_type);
        metadata.addJoin(JoinType.INNERJOIN, dep_type);

        assertEquals(user, metadata.getJoins().get(0).getTarget());
        assertEquals(user_type, metadata.getJoins().get(1).getTarget());
        assertEquals(dep, metadata.getJoins().get(2).getTarget());
        assertEquals(dep_type, metadata.getJoins().get(3).getTarget());
    }

    @Test
    public void Statements_and_Symbols() {
        QueryMetadata metadata = new SortableQueryMetadata();
        QStatement stmt = new QStatement("stmt");
        QSymbol sub = new QSymbol("subject");
        QSymbol obj = new QSymbol("object");
        metadata.addJoin(JoinType.DEFAULT, stmt);
        metadata.addJoin(JoinType.INNERJOIN, sub);
        metadata.addJoin(JoinType.INNERJOIN, obj);

        assertEquals(stmt, metadata.getJoins().get(0).getTarget());
        assertEquals(sub, metadata.getJoins().get(1).getTarget());
        assertEquals(obj, metadata.getJoins().get(2).getTarget());
    }
}
