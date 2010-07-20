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
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PString;

/**
 * QLanguage is a Querydsl query type for QLanguage
 */
@Table("LANGUAGE")
public class QLanguage extends PEntity<QLanguage> {

    private static final long serialVersionUID = -1756160653;

    public static final QLanguage language = new QLanguage("LANGUAGE");

    public final PNumber<Integer> id = createNumber("ID", Integer.class);

    public final PString text = createString("TEXT");

    public final PrimaryKey<QLanguage> primaryKey = new PrimaryKey<QLanguage>(this, id);

    public final ForeignKey<QSymbol> _langKey = new ForeignKey<QSymbol>(this, id, "LANG");

    public QLanguage(String variable) {
        super(QLanguage.class, forVariable(variable));
    }

    public QLanguage(PEntity<? extends QLanguage> entity) {
        super(entity.getType(),entity.getMetadata());
    }

    public QLanguage(PathMetadata<?> metadata) {
        super(QLanguage.class, metadata);
    }

    public Expr<Object[]> all() {
        return CSimple.create(Object[].class, "{0}.*", this);
    }

}

