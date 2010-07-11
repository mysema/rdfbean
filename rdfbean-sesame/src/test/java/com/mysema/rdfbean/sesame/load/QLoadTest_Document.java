package com.mysema.rdfbean.sesame.load;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PString;


/**
 * QDocument is a Querydsl query type for Document
 */
public class QLoadTest_Document extends PEntity<LoadTest.Document> {

    private static final long serialVersionUID = 1255113926;

    public static final QLoadTest_Document document = new QLoadTest_Document("document");

    public final PString id = createString("id");

    public final PString text = createString("text");

    public QLoadTest_Document(String variable) {
        super(LoadTest.Document.class, forVariable(variable));
    }

    public QLoadTest_Document(PEntity<? extends LoadTest.Document> entity) {
        super(entity.getType(),entity.getMetadata());
    }

    public QLoadTest_Document(PathMetadata<?> metadata) {
        super(LoadTest.Document.class, metadata);
    }

}

