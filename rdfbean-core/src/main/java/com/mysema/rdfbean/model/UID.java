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

    private int i;
    
    public UID(String uri) {
        super(uri);
        i = uri.lastIndexOf('#');
        if (i < 0) {
            i = uri.lastIndexOf('/');
        }
        if (i < 0) {
            i = uri.lastIndexOf(':');
        }
        if (i < 0) {
            throw new IllegalArgumentException("No separator character in URI: " + uri);
        }
        i += 1;
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

}
