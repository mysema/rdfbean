package com.mysema.rdfbean.model;

import com.mysema.query.types.PathMetadata;

/**
 * @author tiwe
 *
 */
public class QID extends QNODE<ID>{

    private static final long serialVersionUID = -2696989113637909131L;

    public QID(String variable) {
        super(ID.class, variable);
    }

    public QID(PathMetadata<?> metadata) {
        super(ID.class, metadata);
    }

    public Block a(Object type){
        return Blocks.pattern(this, RDF.type, type);
    }

    public Block has(Object predicate, Object object){
        return Blocks.pattern(this, predicate, object);
    }



}
