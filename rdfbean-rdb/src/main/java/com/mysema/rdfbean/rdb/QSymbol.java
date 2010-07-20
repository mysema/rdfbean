/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.Table;
import com.mysema.query.types.Expr;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.custom.CSimple;
import com.mysema.query.types.path.PBoolean;
import com.mysema.query.types.path.PDateTime;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PString;

/**
 * QSymbol is a Querydsl query type for QSymbol
 */
@Table("SYMBOL")
public class QSymbol extends PEntity<QSymbol> {

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

    public final PrimaryKey<QSymbol> primaryKey = new PrimaryKey<QSymbol>(this, id);

    public final ForeignKey<QLanguage> langKeyFk = new ForeignKey<QLanguage>(this, lang, "ID");

    public final ForeignKey<QStatement> _objectKeyFk = new ForeignKey<QStatement>(this, id, "OBJECT");

    public final ForeignKey<QStatement> _subjectKeyFk = new ForeignKey<QStatement>(this, id, "SUBJECT");

    public final ForeignKey<QStatement> _predicateKeyFk = new ForeignKey<QStatement>(this, id, "PREDICATE");

    public final ForeignKey<QStatement> _modelKeyFk = new ForeignKey<QStatement>(this, id, "MODEL");

    public QSymbol(String variable) {
        super(QSymbol.class, forVariable(variable));
    }

    public QSymbol(PEntity<? extends QSymbol> entity) {
        super(entity.getType(),entity.getMetadata());
    }

    public QSymbol(PathMetadata<?> metadata) {
        super(QSymbol.class, metadata);
    }

    public Expr<Object[]> all() {
        return CSimple.create(Object[].class, "{0}.*", this);
    }

}

