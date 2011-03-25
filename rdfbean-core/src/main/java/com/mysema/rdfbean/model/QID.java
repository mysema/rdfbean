package com.mysema.rdfbean.model;


/**
 * @author tiwe
 *
 */
public class QID extends QResource<ID>{

    private static final long serialVersionUID = -2696989113637909131L;

    public QID(String variable) {
        super(ID.class, variable);
    }

}
