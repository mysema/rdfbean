package com.mysema.rdfbean.model;

/**
 * @author tiwe
 * 
 */
public class QUID extends QResource<UID> {

    private static final long serialVersionUID = -2696989113637909131L;

    public QUID(String variable) {
        super(UID.class, variable);
    }

}
