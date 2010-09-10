/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.Table;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BeanPath;
import com.mysema.query.types.path.PBoolean;
import com.mysema.query.types.path.PDateTime;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PString;

/**
 * QSymbol is a Querydsl query type for QSymbol
 */
@Table("SYMBOL")
public class QSymbol extends RelationalPathBase<QSymbol> {

    private static final long serialVersionUID = 1776011891;

    public static final QSymbol symbol = new QSymbol("symbol");

    public final PBoolean resource = createBoolean("RESOURCE");
    
    public final PNumber<Long> datatype = createNumber("DATATYPE", Long.class);
    
    public final PDateTime<java.util.Date> datetime = createDateTime("DATETIME", java.util.Date.class);

    public final PNumber<Double> floating = createNumber("FLOATING", Double.class);

    public final PNumber<Long> id = createNumber("ID", Long.class);

    public final PNumber<Long> integer = createNumber("INTEGER", Long.class);

    public final PNumber<Integer> lang = createNumber("LANG", Integer.class);

    public final PString lexical = createString("LEXICAL");

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

