package com.mysema.rdfbean.model;

import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.SimplePath;

/**
 * @author tiwe
 *
 */
public class QID extends SimplePath<ID>{

    public static final Constant<UID> RDF_TYPE = new ConstantImpl<UID>(UID.class, RDF.type);

    private static final long serialVersionUID = -2696989113637909131L;

    public QID(String variable) {
        super(ID.class, variable);
    }

    public QID(PathMetadata<?> metadata) {
        super(ID.class, metadata);
    }

    public Block a(Object type){
        return Blocks.pattern(this, RDF_TYPE, type);
    }

    public Block has(Object predicate, Object object){
        return Blocks.pattern(this, predicate, object);
    }

    public Block is(Object predicate, Object subject){
        return Blocks.pattern(subject, predicate, this);
    }

}
