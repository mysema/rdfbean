package com.mysema.rdfbean.model;

public class QResource<T extends ID> extends QNODE<T> {

    private static final long serialVersionUID = -5812253741051256616L;

    public QResource(Class<T> type, String variable) {
        super(type, variable);
    }

    public PatternBlock a(Object type){
        return Blocks.pattern(this, RDF.type, type);
    }

    public PatternBlock has(Object predicate, Object object){
        return Blocks.pattern(this, predicate, object);
    }


}
