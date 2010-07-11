package com.mysema.rdfbean.sesame.load;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PString;
import com.mysema.query.types.path.PathInits;


/**
 * QRevision is a Querydsl query type for Revision
 */
public class QLoadTest_Revision extends PEntity<LoadTest.Revision> {

    private static final long serialVersionUID = 132606854;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QLoadTest_Revision revision = new QLoadTest_Revision("revision");

    public final PNumber<Long> created = createNumber("created", Long.class);

    public final PString id = createString("id");

    public final QLoadTest_Entity revisionOf;

    public final PNumber<Long> svnRevision = createNumber("svnRevision", Long.class);

    public QLoadTest_Revision(String variable) {
        this(LoadTest.Revision.class, forVariable(variable), INITS);
    }

    public QLoadTest_Revision(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QLoadTest_Revision(PathMetadata<?> metadata, PathInits inits) {
        this(LoadTest.Revision.class, metadata, inits);
    }

    public QLoadTest_Revision(Class<? extends LoadTest.Revision> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.revisionOf = inits.isInitialized("revisionOf") ? new QLoadTest_Entity(forProperty("revisionOf"), inits.get("revisionOf")) : null;
    }

}

