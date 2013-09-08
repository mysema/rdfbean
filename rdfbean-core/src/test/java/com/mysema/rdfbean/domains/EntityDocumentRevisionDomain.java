/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface EntityDocumentRevisionDomain {

    @ClassMapping
    public static class Revision {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public long svnRevision;

        @Predicate
        public long created;

        @Predicate
        public Entity revisionOf;

        public long getSvnRevision() {
            return svnRevision;
        }

        public long getCreated() {
            return created;
        }

        public Entity getRevisionOf() {
            return revisionOf;
        }

    }

    @ClassMapping
    public static class Entity {

        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public Document document;

        public String getId() {
            return id;
        }

        public Document getDocument() {
            return document;
        }

    }

    @ClassMapping
    public static class Document {

        @Id(IDType.LOCAL)
        public String id;

        public String getId() {
            return id;
        }

    }

    public class QDocument extends EntityPathBase<EntityDocumentRevisionDomain.Document> {

        private static final long serialVersionUID = 539301614;

        public static final QDocument document = new QDocument("document");

        public final StringPath id = createString("id");

        public QDocument(String variable) {
            super(EntityDocumentRevisionDomain.Document.class, forVariable(variable));
        }

        public QDocument(EntityPathBase<? extends EntityDocumentRevisionDomain.Document> entity) {
            super(entity.getType(), entity.getMetadata());
        }

        public QDocument(PathMetadata<?> metadata) {
            super(EntityDocumentRevisionDomain.Document.class, metadata);
        }

    }

    public class QEntity extends EntityPathBase<EntityDocumentRevisionDomain.Entity> {

        private static final long serialVersionUID = -1236041098;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QEntity entity = new QEntity("entity");

        public final QDocument document;

        public final StringPath id = createString("id");

        public QEntity(String variable) {
            this(EntityDocumentRevisionDomain.Entity.class, forVariable(variable), INITS);
        }

        public QEntity(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QEntity(PathMetadata<?> metadata, PathInits inits) {
            this(EntityDocumentRevisionDomain.Entity.class, metadata, inits);
        }

        public QEntity(Class<? extends EntityDocumentRevisionDomain.Entity> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.document = inits.isInitialized("document") ? new QDocument(forProperty("document")) : null;
        }

    }

    public class QRevision extends EntityPathBase<EntityDocumentRevisionDomain.Revision> {

        private static final long serialVersionUID = -583205458;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QRevision revision = new QRevision("revision");

        public final NumberPath<Long> created = createNumber("created", Long.class);

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final QEntity revisionOf;

        public final NumberPath<Long> svnRevision = createNumber("svnRevision", Long.class);

        public QRevision(String variable) {
            this(EntityDocumentRevisionDomain.Revision.class, forVariable(variable), INITS);
        }

        public QRevision(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QRevision(PathMetadata<?> metadata, PathInits inits) {
            this(EntityDocumentRevisionDomain.Revision.class, metadata, inits);
        }

        public QRevision(Class<? extends EntityDocumentRevisionDomain.Revision> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.revisionOf = inits.isInitialized("revisionOf") ? new QEntity(forProperty("revisionOf"), inits.get("revisionOf")) : null;
        }

    }

}
