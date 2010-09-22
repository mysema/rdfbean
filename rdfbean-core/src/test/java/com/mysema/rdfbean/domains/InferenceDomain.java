/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;

import static com.mysema.query.types.PathMetadataFactory.*;

public interface InferenceDomain {

    @ClassMapping(ns=TEST.NS)
    public static class Entity1{
        @Id
        public String id;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity2 extends Entity1{
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity3 extends Entity2{
        
    }
    
    public class QEntity1 extends EntityPathBase<InferenceDomain.Entity1> {

        private static final long serialVersionUID = -1404735526;

        public static final QEntity1 entity1 = new QEntity1("entity1");

        public final StringPath id = createString("id");

        public QEntity1(String variable) {
            super(InferenceDomain.Entity1.class, forVariable(variable));
        }

        public QEntity1(EntityPathBase<? extends InferenceDomain.Entity1> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity1(PathMetadata<?> metadata) {
            super(InferenceDomain.Entity1.class, metadata);
        }

    }
    
    public class QEntity2 extends EntityPathBase<InferenceDomain.Entity2> {

        private static final long serialVersionUID = -1404735525;

        public static final QEntity2 entity2 = new QEntity2("entity2");

        public final QEntity1 _super = new QEntity1(this);

        //inherited
        public final StringPath id = _super.id;

        public QEntity2(String variable) {
            super(InferenceDomain.Entity2.class, forVariable(variable));
        }

        public QEntity2(EntityPathBase<? extends InferenceDomain.Entity2> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity2(PathMetadata<?> metadata) {
            super(InferenceDomain.Entity2.class, metadata);
        }

    }
    
    public class QEntity3 extends EntityPathBase<InferenceDomain.Entity3> {

        private static final long serialVersionUID = -1404735524;

        public static final QEntity3 entity3 = new QEntity3("entity3");

        public final QEntity2 _super = new QEntity2(this);

        //inherited
        public final StringPath id = _super.id;

        public QEntity3(String variable) {
            super(InferenceDomain.Entity3.class, forVariable(variable));
        }

        public QEntity3(EntityPathBase<? extends InferenceDomain.Entity3> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity3(PathMetadata<?> metadata) {
            super(InferenceDomain.Entity3.class, metadata);
        }

    }
    
}
