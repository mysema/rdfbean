/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PString;
import com.mysema.query.types.path.PathInits;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface EntityRevisionTermDomain {
    

    @ClassMapping(ns=TEST.NS)
    public static class EntityRevision{
        
        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public String text;
        
        @Predicate
        public Entity revisionOf;

        public Entity getRevisionOf() {
            return revisionOf;
        }
        
        public String getText(){
            return text;
        }

    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity{
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public EntityRevision latestRevision;
                
        @Predicate
        public Term term;
        
        public Term getTerm() {
            return term;
        }

        public EntityRevision getLatestRevision() {
            return latestRevision;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Term{

        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public String text2;
     
        public String getText2(){
            return text2;
        }
   
    }
    
    public class QEntity extends EntityPathBase<EntityRevisionTermDomain.Entity> {

        private static final long serialVersionUID = -1998702918;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QEntity entity = new QEntity("entity");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final QEntityRevision latestRevision;

        public final QTerm term;

        public QEntity(String variable) {
            this(EntityRevisionTermDomain.Entity.class, forVariable(variable), INITS);
        }

        public QEntity(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QEntity(PathMetadata<?> metadata, PathInits inits) {
            this(EntityRevisionTermDomain.Entity.class, metadata, inits);
        }

        public QEntity(Class<? extends EntityRevisionTermDomain.Entity> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.latestRevision = inits.isInitialized("latestRevision") ? new QEntityRevision(forProperty("latestRevision"), inits.get("latestRevision")) : null;
            this.term = inits.isInitialized("term") ? new QTerm(forProperty("term")) : null;
        }

    }
    
    public class QEntityRevision extends EntityPathBase<EntityRevisionTermDomain.EntityRevision> {

        private static final long serialVersionUID = -397412171;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QEntityRevision entityRevision = new QEntityRevision("entityRevision");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final QEntity revisionOf;

        public final PString text = createString("text");

        public QEntityRevision(String variable) {
            this(EntityRevisionTermDomain.EntityRevision.class, forVariable(variable), INITS);
        }

        public QEntityRevision(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QEntityRevision(PathMetadata<?> metadata, PathInits inits) {
            this(EntityRevisionTermDomain.EntityRevision.class, metadata, inits);
        }

        public QEntityRevision(Class<? extends EntityRevisionTermDomain.EntityRevision> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.revisionOf = inits.isInitialized("revisionOf") ? new QEntity(forProperty("revisionOf"), inits.get("revisionOf")) : null;
        }

    }

    public class QTerm extends EntityPathBase<EntityRevisionTermDomain.Term> {

        private static final long serialVersionUID = 1196122371;

        public static final QTerm term = new QTerm("term");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PString text2 = createString("text2");

        public QTerm(String variable) {
            super(EntityRevisionTermDomain.Term.class, forVariable(variable));
        }

        public QTerm(EntityPathBase<? extends EntityRevisionTermDomain.Term> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QTerm(PathMetadata<?> metadata) {
            super(EntityRevisionTermDomain.Term.class, metadata);
        }

    }
    
    
}
