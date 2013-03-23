package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

/**
 * Dialect provides a generic service for RDF node creation and conversion
 * 
 * @author Samppa
 * @author Timo
 */
public interface Dialect<N, R extends N, B extends R, U extends R, L extends N, S> {

    B createBNode();

    S createStatement(R subject, U predicate, N object);

    S createStatement(R subject, U predicate, N object, @Nullable U context);

    BID getBID(B bnode);

    B getBNode(BID bid);

    ID getID(R resource);

    LIT getLIT(L literal);

    L getLiteral(LIT lit);

    N getNode(NODE node);

    NODE getNODE(N node);

    NodeType getNodeType(N node);

    N getObject(S statement);

    U getPredicate(S statement);

    R getResource(ID id);

    R getSubject(S statement);

    UID getUID(U resource);

    U getURI(UID uid);

}