package com.mysema.rdfbean.sesame.load;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PString;
import com.mysema.query.types.path.PathInits;


/**
 * QEntity is a Querydsl query type for Entity
 */
public class QLoadTest_Entity extends PEntity<LoadTest.Entity> {

    private static final long serialVersionUID = -672168370;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QLoadTest_Entity entity = new QLoadTest_Entity("entity");

    public final QLoadTest_Document document;

    public final PString id = createString("id");

    public final PString text = createString("text");

    public QLoadTest_Entity(String variable) {
        this(LoadTest.Entity.class, forVariable(variable), INITS);
    }

    public QLoadTest_Entity(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QLoadTest_Entity(PathMetadata<?> metadata, PathInits inits) {
        this(LoadTest.Entity.class, metadata, inits);
    }

    public QLoadTest_Entity(Class<? extends LoadTest.Entity> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QLoadTest_Document(forProperty("document")) : null;
    }

}

