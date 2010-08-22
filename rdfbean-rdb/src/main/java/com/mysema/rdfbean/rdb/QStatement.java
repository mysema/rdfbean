/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.Table;
import com.mysema.query.types.Expr;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.custom.CSimple;
import com.mysema.query.types.path.BeanPath;
import com.mysema.query.types.path.PNumber;

/**
 * QStatement is a Querydsl query type for QStatement
 */
@Table("STATEMENT")
public class QStatement extends BeanPath<QStatement> implements RelationalPath<QStatement>{

    private static final long serialVersionUID = 2085085876;

    public static final QStatement statement = new QStatement("stmt");

    public final PNumber<Long> model = createNumber("MODEL", Long.class);

    public final PNumber<Long> object = createNumber("OBJECT", Long.class);

    public final PNumber<Long> predicate = createNumber("PREDICATE", Long.class);

    public final PNumber<Long> subject = createNumber("SUBJECT", Long.class);

    public final PrimaryKey<QStatement> primaryKey = new PrimaryKey<QStatement>(this, model, object, predicate, subject);

    public final ForeignKey<QSymbol> objectFk = new ForeignKey<QSymbol>(this, object, "ID");

    public final ForeignKey<QSymbol> subjectFk = new ForeignKey<QSymbol>(this, subject, "ID");

    public final ForeignKey<QSymbol> predicateFk = new ForeignKey<QSymbol>(this, predicate, "ID");

    public final ForeignKey<QSymbol> modelFk = new ForeignKey<QSymbol>(this, model, "ID");

    public QStatement(String variable) {
        super(QStatement.class, forVariable(variable));
    }

    public QStatement(BeanPath<? extends QStatement> entity) {
        super(entity.getType(),entity.getMetadata());
    }

    public QStatement(PathMetadata<?> metadata) {
        super(QStatement.class, metadata);
    }

    public Expr<Object[]> all() {
        return CSimple.create(Object[].class, "{0}.*", this);
    }

    @Override
    public Collection<ForeignKey<?>> getForeignKeys() {
        return Arrays.<ForeignKey<?>>asList(subjectFk, predicateFk, objectFk, modelFk);
    }

    @Override
    public Collection<ForeignKey<?>> getInverseForeignKeys() {
        return Collections.emptyList();
    }

    @Override
    public PrimaryKey<QStatement> getPrimaryKey() {
        return primaryKey;
    }
}

