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
import com.mysema.query.sql.Table;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BeanPath;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

/**
 * QSymbol is a Querydsl query type for QSymbol
 */
@Table("SYMBOL")
public class QSymbol extends RelationalPathBase<QSymbol> {

    private static final long serialVersionUID = 1776011891;

    public static final QSymbol symbol = new QSymbol("symbol");

    public final BooleanPath resource = createBoolean("RESOURCE");

    public final NumberPath<Long> datatype = createNumber("DATATYPE", Long.class);

    public final DateTimePath<java.sql.Timestamp> datetimeval = createDateTime("DATETIMEVAL", java.sql.Timestamp.class);

    public final NumberPath<Double> floatval = createNumber("FLOATVAL", Double.class);

    public final NumberPath<Long> id = createNumber("ID", Long.class);

    public final NumberPath<Integer> lang = createNumber("LANG", Integer.class);

    public final StringPath lexical = createString("LEXICAL");

    public final PrimaryKey<QSymbol> primaryKey = createPrimaryKey(id);

    public final ForeignKey<QLanguage> langKeyFk = createForeignKey(lang, "ID");

    public final ForeignKey<QStatement> _objectKeyFk = createForeignKey(id, "OBJECT");

    public final ForeignKey<QStatement> _subjectKeyFk = createForeignKey(id, "SUBJECT");

    public final ForeignKey<QStatement> _predicateKeyFk = createForeignKey(id, "PREDICATE");

    public final ForeignKey<QStatement> _modelKeyFk = createForeignKey(id, "MODEL");

    public QSymbol(String variable) {
        super(QSymbol.class, forVariable(variable));
    }

    public QSymbol(BeanPath<? extends QSymbol> entity) {
        super(entity.getType(),entity.getMetadata());
    }

    public QSymbol(PathMetadata<?> metadata) {
        super(QSymbol.class, metadata);
    }

}

