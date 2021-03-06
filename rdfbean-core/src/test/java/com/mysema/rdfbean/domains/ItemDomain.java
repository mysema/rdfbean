/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;

public interface ItemDomain {

    @ClassMapping(ns = TEST.NS, ln = "MemoryStoreTest_Item")
    public static class Item {

        @Id
        public ID resource;

        @Predicate(ln = "path")
        public String path;

        public String getPath() {
            return path;
        }

        public void setPath(String id) {
            this.path = id;
        }

        public ID getResource() {
            return resource;
        }

        public void setResource(ID resource) {
            this.resource = resource;
        }

    }

    public class QItem extends EntityPathBase<Item> {

        private static final long serialVersionUID = 1916166668;

        public static final QItem item = new QItem("item");

        public final StringPath path = createString("path");

        public final SimplePath<com.mysema.rdfbean.model.ID> resource = createSimple("resource", com.mysema.rdfbean.model.ID.class);

        public QItem(String variable) {
            super(Item.class, forVariable(variable));
        }

        public QItem(EntityPathBase<? extends Item> entity) {
            super(entity.getType(), entity.getMetadata());
        }

        public QItem(PathMetadata<?> metadata) {
            super(Item.class, metadata);
        }

    }

}
