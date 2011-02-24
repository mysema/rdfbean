/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.map.LRUMap;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.AbstractDialect;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.NodeType;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * SesameDialect is a Dialect implementation for Sesame
 *
 * @author sasa
 *
 */
public class SesameDialect extends AbstractDialect<Value, Resource, BNode, URI, Literal, Statement> {

    private static final int CACHE_SIZE = 2048;

    private final Map<BNode, BID> bidCache = new HashMap<BNode, BID>(CACHE_SIZE);

    private final Map<Literal, LIT> litCache = new HashMap<Literal, LIT>(CACHE_SIZE);

    private final Map<URI, UID> uidCache = new HashMap<URI, UID>(CACHE_SIZE);

    private final Map<BID, BNode> bnodeCache = new LRUMap<BID, BNode>(CACHE_SIZE);

    private final Map<LIT, Literal> literalCache = new LRUMap<LIT, Literal>(CACHE_SIZE);

    private final Map<UID, URI> uriCache = new LRUMap<UID, URI>(CACHE_SIZE);

    private final ValueFactory vf;

    public SesameDialect(ValueFactory vf) {
        Assert.notNull(vf,"vf");
        this.vf = vf;
    }

    public void clear(){
        uidCache.clear();
        bidCache.clear();
        litCache.clear();

        uriCache.clear();
        bnodeCache.clear();
        literalCache.clear();
    }

    @Override
    public BNode createBNode() {
        return vf.createBNode();
    }

    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object) {
        return vf.createStatement(subject, predicate, object);
    }

    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object, @Nullable URI context) {
        return vf.createStatement(subject, predicate, object, context);
    }

    @Override
    public BID getBID(BNode bnode) {
        BID bid = bidCache.get(bnode);
        if (bid == null) {
            bid = new BID(bnode.getID());
            bidCache.put(bnode, bid);
            bnodeCache.put(bid, bnode);
        }
        return bid;
    }

    @Override
    public BNode getBNode(BID bid) {
        BNode bnode = bnodeCache.get(bid);
        if (bnode == null){
            bnode = vf.createBNode(bid.getId());
            bnodeCache.put(bid, bnode);
            bidCache.put(bnode, bid);
        }
        return bnode;
    }

    @Override
    public ID getID(Resource resource) {
        if (resource instanceof URI) {
            return getUID((URI) resource);
        } else if (resource instanceof BNode){
            return getBID((BNode) resource);
        }else{
            throw new IllegalArgumentException("Expected URI or BNode, got " + resource);
        }
    }

    @Override
    public LIT getLIT(Literal literal) {
        LIT lit = litCache.get(literal);
        if (lit == null) {
            if (literal.getLanguage() != null) {
                lit = new LIT(literal.stringValue(), literal.getLanguage());
            } else if (literal.getDatatype() != null) {
                lit = new LIT(literal.stringValue(), getDatatypeUID(literal.getDatatype().stringValue()));
            } else {
                lit = new LIT(literal.stringValue(), RDF.text);
            }
            litCache.put(literal, lit);
        }
        return lit;
    }

    @Override
    public Literal getLiteral(LIT lit) {
        Literal literal = literalCache.get(lit);
        if (literal == null){
            if (lit.isText()) {
                Locale lang = lit.getLang();
                if (lang.equals(Locale.ROOT)) {
                    literal = vf.createLiteral(lit.getValue());
                } else {
                    literal = vf.createLiteral(lit.getValue(),  LocaleUtil.toLang(lang));
                }
            } else if (lit.isString()) {
                literal = vf.createLiteral(lit.getValue(), getURI(XSD.stringType));
            } else if (lit.getDatatype().equals(RDF.text)){
                literal = vf.createLiteral(lit.getValue());
            } else {
                literal = vf.createLiteral(lit.getValue(), getURI(lit.getDatatype()));
            }
            literalCache.put(lit, literal);
        }
        return literal;
    }

    @Override
    public NODE getNODE(Value node) {
        if (node instanceof Resource) {
            return getID((Resource) node);
        } else  if (node instanceof Literal){
            return getLIT((Literal) node);
        }else{
            throw new IllegalArgumentException("Expected Resource or Literal, got " + node);
        }
    }

    @Override
    public NodeType getNodeType(Value node) {
        if (node instanceof Resource) {
            if (node instanceof URI) {
                return NodeType.URI;
            } else {
                return NodeType.BLANK;
            }
        } else {
            return NodeType.LITERAL;
        }
    }

    @Override
    public Value getObject(Statement statement) {
        return statement.getObject();
    }

    @Override
    public URI getPredicate(Statement statement) {
        return statement.getPredicate();
    }

    @Override
    public Resource getSubject(Statement statement) {
        return statement.getSubject();
    }

    @Override
    public UID getUID(URI uri) {
        UID uid = uidCache.get(uri);
        if (uid == null) {
            uid = new UID(uri.getNamespace(), uri.getLocalName());
            uidCache.put(uri, uid);
        }
        return uid;
    }

    @Override
    public URI getURI(UID uid) {
        URI uri = uriCache.get(uid);
        if (uri == null){
            uri = vf.createURI(uid.ns(), uid.ln());
            uriCache.put(uid, uri);
        }
        return uri;
    }

    public ValueFactory getValueFactory(){
        return vf;
    }

}
