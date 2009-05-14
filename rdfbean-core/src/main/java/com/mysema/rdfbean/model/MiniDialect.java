/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;

/**
 * @author sasa
 * 
 */
public final class MiniDialect extends Dialect<NODE, ID, BID, UID, LIT, STMT> {

    public static LIT LIT(String value) {
        return new LIT(value);
    }

    public static LIT LIT(String value, Locale locale) {
        return new LIT(value, locale);
    }

    public static LIT LIT(String value, String lang) {
        return new LIT(value, lang);
    }

    public static LIT LIT(String value, UID datatype) {
        return new LIT(value, datatype);
    }

    public static STMT STMT(ID subject, UID predicate, NODE object) {
        return new STMT(subject, predicate, object);
    }

    public static UID UID(String ns, String ln) {
        return new UID(ns, ln);
    }
    
    public static UID UID(String uri) {
        return new UID(uri);
    }
    
    public static UID UID(String parentNs, String ns, String ln,
            String elementName) {
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

    @Override
    public BID createBNode() {
        return new BID();
    }

    @Override
    public STMT createStatement(ID subject, UID predicate, NODE object) {
        return new STMT(subject, predicate, object);
    }

    @Override
    public BID getBID(BID bnode) {
        return bnode;
    }

    @Override
    public BID getBNode(BID bid) {
        return bid;
    }

    @Override
    public ID getID(ID resource) {
        return resource;
    }

    @Override
    public LIT getLIT(LIT literal) {
        return literal;
    }

    @Override
    public LIT getLiteral(LIT lit) {
        return lit;
    }

    @Override
    public LIT getLiteral(String value) {
        return LIT(value);
    }

    @Override
    public LIT getLiteral(String value, Locale language) {
        return LIT(value, language);
    }

    @Override
    public LIT getLiteral(String value, UID datatype) {
        return LIT(value, datatype);
    }
    
    @Override
    public NODE getNode(NODE node) {
        return node;
    }

    @Override
    public NodeType getNodeType(NODE node) {
        return node.getNodeType();
    }

    @Override
    public NODE getObject(STMT statement) {
        return statement.getObject();
    }

    @Override
    public UID getPredicate(STMT statement) {
        return statement.getPredicate();
    }

    @Override
    public ID getSubject(STMT statement) {
        return statement.getSubject();
    }

    @Override
    public UID getUID(UID resource) {
        return resource;
    }

    @Override
    public UID getURI(UID uid) {
        return uid;
    }

    @Override
    public UID getURI(String uri) {
        return UID(uri);
    }
   
}
