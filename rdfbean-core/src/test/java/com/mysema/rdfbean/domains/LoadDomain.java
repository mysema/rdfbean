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
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

public interface LoadDomain {

    @ClassMapping
    public class Document {
        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public String text;
    }

    @ClassMapping
    public class Entity {
        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public Document document;

        @Predicate
        public String text;

    }

    @ClassMapping
    public class Revision {
        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public long created;

        @Predicate
        public Entity revisionOf;

        @Predicate
        public long svnRevision;

    }

    public class QDocument extends EntityPathBase<Document> {

        private static final long serialVersionUID = 1255113926;

        public static final QDocument document = new QDocument("document");

        public final StringPath id = createString("id");

        public final StringPath text = createString("text");

        public QDocument(String variable) {
            super(Document.class, forVariable(variable));
        }

        public QDocument(EntityPathBase<? extends Document> entity) {
            super(entity.getType(), entity.getMetadata());
        }

        public QDocument(PathMetadata<?> metadata) {
            super(Document.class, metadata);
        }

    }

    public class QEntity extends EntityPathBase<Entity> {

        private static final long serialVersionUID = -672168370;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QEntity entity = new QEntity("entity");

        public final QDocument document;

        public final StringPath id = createString("id");

        public final StringPath text = createString("text");

        public QEntity(String variable) {
            this(Entity.class, forVariable(variable), INITS);
        }

        public QEntity(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QEntity(PathMetadata<?> metadata, PathInits inits) {
            this(Entity.class, metadata, inits);
        }

        public QEntity(Class<? extends Entity> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.document = inits.isInitialized("document") ? new QDocument(forProperty("document")) : null;
        }

    }

    public class QRevision extends EntityPathBase<Revision> {

        private static final long serialVersionUID = 132606854;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QRevision revision = new QRevision("revision");

        public final NumberPath<Long> created = createNumber("created", Long.class);

        public final StringPath id = createString("id");

        public final QEntity revisionOf;

        public final NumberPath<Long> svnRevision = createNumber("svnRevision", Long.class);

        public QRevision(String variable) {
            this(Revision.class, forVariable(variable), INITS);
        }

        public QRevision(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QRevision(PathMetadata<?> metadata, PathInits inits) {
            this(Revision.class, metadata, inits);
        }

        public QRevision(Class<? extends Revision> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.revisionOf = inits.isInitialized("revisionOf") ? new QEntity(forProperty("revisionOf"), inits.get("revisionOf")) : null;
        }

    }

}
