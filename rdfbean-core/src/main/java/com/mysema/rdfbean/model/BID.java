/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.UUID;

import com.mysema.commons.lang.Assert;
import com.mysema.query.annotations.Entity;

/**
 * @author sasa
 *
 */
@Entity
public final class BID extends ID {

    private static final long serialVersionUID = 4477657161877734394L;

    public BID() {
        this(UUID.randomUUID().toString());
    }
    
    public BID(String bnodeId) {
        super(Assert.hasText(bnodeId));
    }
    
    public String toString() {
        return "_:" + id;
    }

	@Override
	public NodeType getNodeType() {
		return NodeType.BLANK;
	}

}
