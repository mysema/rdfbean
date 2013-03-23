/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * MiniDialect is the Dialect implementation for the MiniRepository
 * 
 * @author sasa
 * 
 */
public final class MiniDialect extends AbstractDialect<NODE, ID, BID, UID, LIT, STMT> {

    @Override
    public BID createBNode() {
        return new BID();
    }

    @Override
    public STMT createStatement(ID subject, UID predicate, NODE object) {
        return new STMT(subject, predicate, object, null, true);
    }

    @Override
    public STMT createStatement(ID subject, UID predicate, NODE object, UID context) {
        return new STMT(subject, predicate, object, context, true);
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
    public NODE getNODE(NODE node) {
        return node;
    }

    @Override
    public ID getResource(ID id) {
        return id;
    }

}
