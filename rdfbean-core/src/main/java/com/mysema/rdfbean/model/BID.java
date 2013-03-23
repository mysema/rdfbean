/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import com.mysema.commons.lang.Assert;

/**
 * BID represents a blank node
 * 
 * @author sasa
 * 
 */
@Immutable
public final class BID extends ID {

    private static final long serialVersionUID = 4477657161877734394L;

    public BID() {
        this("b" + UUID.randomUUID().toString());
    }

    public BID(String bnodeId) {
        super(Assert.hasText(bnodeId, "bnodeId"));
    }

    @Override
    public String toString() {
        return "_:" + id;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.BLANK;
    }

    @Override
    public boolean isBNode() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isResource() {
        return true;
    }

    @Override
    public boolean isURI() {
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof BID) {
            return this.id.equals(((BID) obj).id);
        } else {
            return false;
        }
    }

    @Override
    public BID asBNode() {
        return this;
    }
}
