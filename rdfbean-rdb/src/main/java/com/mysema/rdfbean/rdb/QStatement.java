/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BeanPath;
import com.mysema.query.types.path.NumberPath;

/**
 * QStatement is a Querydsl query type for QStatement
 */
public class QStatement extends RelationalPathBase<QStatement>{

    private static final long serialVersionUID = 2085085876;

    public static final QStatement statement = new QStatement("stmt");

    public final NumberPath<Long> model = createNumber("MODEL", Long.class);

    public final NumberPath<Long> object = createNumber("OBJECT", Long.class);

    public final NumberPath<Long> predicate = createNumber("PREDICATE", Long.class);

    public final NumberPath<Long> subject = createNumber("SUBJECT", Long.class);

    public final PrimaryKey<QStatement> primaryKey = createPrimaryKey(model, object, predicate, subject);

    public final ForeignKey<QSymbol> objectFk = createForeignKey(object, "ID");

    public final ForeignKey<QSymbol> subjectFk = createForeignKey(subject, "ID");

    public final ForeignKey<QSymbol> predicateFk = createForeignKey(predicate, "ID");

    public final ForeignKey<QSymbol> modelFk = createForeignKey(model, "ID");

    public QStatement(String variable) {
        super(QStatement.class, forVariable(variable), null, "STATEMENT");
    }

    public QStatement(BeanPath<? extends QStatement> entity) {
        super(entity.getType(),entity.getMetadata(), null, "STATEMENT");
    }

    public QStatement(PathMetadata<?> metadata) {
        super(QStatement.class, metadata, null, "STATEMENT");
    }

}

