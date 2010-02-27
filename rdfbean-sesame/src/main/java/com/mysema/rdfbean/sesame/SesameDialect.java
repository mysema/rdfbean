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

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
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
public class SesameDialect extends Dialect<Value, Resource, BNode, URI, Literal, Statement> {
    
    private final Map<BNode, BID> bnodeCache = new HashMap<BNode, BID>(1024);
    
    private final Map<Literal, LIT> literalCache = new HashMap<Literal, LIT>(1024); 
    
    private final Map<URI, UID> uriCache = new HashMap<URI, UID>(1024); 
        
    private final ValueFactory vf; 

    public SesameDialect(ValueFactory vf) {
        Assert.notNull(vf);
        this.vf = vf;
    }

    public void clear(){
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
        BID bid = bnodeCache.get(bnode);
        if (bid == null) {
            bid = new BID(bnode.getID());
            bnodeCache.put(bnode, bid);
        }
        return bid;
    }

    @Override
    public BNode getBNode(BID bid) {
        return vf.createBNode(bid.getId());
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
        LIT lit = literalCache.get(literal);
        if (lit == null) {
            if (literal.getLanguage() != null) {
                lit = new LIT(literal.stringValue(), literal.getLanguage());
            } else if (literal.getDatatype() != null) {
                lit = new LIT(literal.stringValue(), getDatatypeUID(literal.getDatatype().stringValue()));
            } else {
                lit = new LIT(literal.stringValue(), RDF.text);
            }
            literalCache.put(literal, lit);
        }
        return lit;
    }

    @Override
    public Literal getLiteral(LIT lit) {
        if (lit.isText()) {
            Locale lang = lit.getLang();
            if (lang.equals(Locale.ROOT)) {
                return vf.createLiteral(lit.getValue());
            } else {
                return vf.createLiteral(lit.getValue(),  LocaleUtil.toLang(lang));
            }
        } else if (lit.isString()) {
            return vf.createLiteral(lit.getValue(), getURI(XSD.stringType));
        } else if (lit.getDatatype().equals(RDF.text)){    
            return vf.createLiteral(lit.getValue());
        } else {
            return vf.createLiteral(lit.getValue(), getURI(lit.getDatatype()));
        }
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
        UID uid = uriCache.get(uri);
        if (uid == null) {
            uid = new UID(uri.getNamespace(), uri.getLocalName());
            uriCache.put((URI) uri, uid);
        }
        return uid;
    }
    
    @Override
    public URI getURI(UID uid) {
        return vf.createURI(uid.ns(), uid.ln());
    }
    
    public ValueFactory getValueFactory(){
        return vf;
    }

}
