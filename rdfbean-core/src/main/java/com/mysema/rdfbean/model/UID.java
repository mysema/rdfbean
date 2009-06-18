/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.Assert;
import com.mysema.query.annotations.Entity;

/**
 * @author sasa
 *
 */
@Entity
public final class UID extends ID {

    private static final long serialVersionUID = -5243644990902193387L;

    private final int i;
    
    public UID(String uri) {
        super(uri);
        int tmp = uri.lastIndexOf('#');
        if (tmp < 0) {
            tmp = uri.lastIndexOf('/');
        }
        if (tmp < 0) {
            tmp = uri.lastIndexOf(':');
        }
        if (tmp < 0) {
            throw new IllegalArgumentException("No separator character in URI: " + uri);
        }
        tmp += 1;
        this.i = tmp;
    }
    
    public UID(String ns, String ln) {
        super(Assert.hasText(ns) + Assert.notNull(ln));
        i = ns.length();
    }
    
    public String ln() {
        if (i < id.length()) {
            return id.substring(i);
        } else {
            return "";
        }
    }

    public String ns() {
        if (i > 0) {
            return id.substring(0, i);
        } else {
            return id;
        }
    }
    
    public String getNamespace() {
        return ns();
    }

    public String getLocalName() {
        return ln();
    }
    
    @Override
	public NodeType getNodeType() {
		return NodeType.URI;
	}

    @Override
    public boolean isBNode() {
        return false;
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
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof UID) {
            // id is interned!
            return this.id == ((UID) obj).id;
        } else {
            return false;
        }
    }

}
