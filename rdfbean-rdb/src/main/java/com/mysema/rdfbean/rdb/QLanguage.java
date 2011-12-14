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
import com.mysema.query.types.path.StringPath;

/**
 * QLanguage is a Querydsl query type for QLanguage
 */
public class QLanguage extends RelationalPathBase<QLanguage>{

    private static final long serialVersionUID = -1756160653;

    public static final QLanguage language = new QLanguage("lang");

    public final NumberPath<Integer> id = createNumber("ID", Integer.class);

    public final StringPath text = createString("TEXT");

    public final PrimaryKey<QLanguage> primaryKey = createPrimaryKey(id);

    public final ForeignKey<QSymbol> _langKey = createForeignKey(id, "LANG");

    public QLanguage(String variable) {
        super(QLanguage.class, forVariable(variable), null, "LANGUAGE");
    }

    public QLanguage(BeanPath<? extends QLanguage> entity) {
        super(entity.getType(),entity.getMetadata(), null, "LANGUAGE");
    }

    public QLanguage(PathMetadata<?> metadata) {
        super(QLanguage.class, metadata, null, "LANGUAGE");
    }

}

