/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import com.mysema.commons.lang.Assert;

/**
 * UID represents an URI resource
 * 
 * @author sasa
 *
 */
@Immutable
public final class UID extends ID {

    private static final long serialVersionUID = -5243644990902193387L;

    private final int i;
    
    @Nullable
    private transient String ns, ln;
    
    public static UID create(@Nullable String parentNs, String ns, String ln, @Nullable String elementName) {
        if (isBlank(ns)) {
            if (isNotBlank(parentNs)) {
                ns = parentNs;
            } else {
                ns = "";
            }
        }
        if (isBlank(ln)) {
            ln = elementName;
        }
        if (isBlank(ln)) {
            throw new IllegalArgumentException("Cannot resolve");
        }
        return new UID(ns, ln);
    }
    
    public UID(String uri) {
        super(Assert.hasText(uri,"uri").intern());
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
    
    public UID(String namespace, String localeName) {
        super((Assert.hasText(namespace,"ns") + Assert.notNull(localeName,"ln")).intern());
        i = namespace.length();
        this.ns = namespace;
        this.ln = localeName;
    }
    
    public String ln() {
        if (ln == null){
            if (i < id.length()) {
                ln = id.substring(i);
            } else {
                ln = "";
            }    
        }
        return ln;        
    }

    public String ns() {
        if (ns == null){
            if (i > 0) {
                ns = id.substring(0, i);
            } else {
                ns = id;
            } 
        }
        return ns;
        
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
    
    @Override
    public UID asURI(){
        return this;
    }

}
