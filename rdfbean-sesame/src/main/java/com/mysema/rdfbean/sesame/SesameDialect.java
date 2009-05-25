/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.util.Locale;

import org.openrdf.model.*;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.*;

/**
 * @author sasa
 *
 */
public class SesameDialect extends Dialect<Value, Resource, BNode, URI, Literal, Statement> {
    
    private ValueFactory vf;

    public SesameDialect(ValueFactory vf) {
        Assert.notNull(vf);
        this.vf = vf;
    }

    @Override
    public BNode createBNode() {
        return vf.createBNode();
    }

    @Override
    public Statement createStatement(Resource subject, URI predicate,
            Value object) {
        return vf.createStatement(subject, predicate, object);
    }

    @Override
    public BID getBID(BNode bnode) {
        return new BID(bnode.getID());
    }

    @Override
    public BNode getBNode(BID bid) {
        return vf.createBNode(bid.getId());
    }

    @Override
    public ID getID(Resource resource) {
        if (resource instanceof URI) {
            URI uri = (URI) resource;
            return new UID(uri.getNamespace(), uri.getLocalName());
        } else {
            return new BID(((BNode) resource).getID());
        }
    }

    @Override
    public LIT getLIT(Literal literal) {
        if (literal.getLanguage() != null) {
            return new LIT(literal.stringValue(), literal.getLanguage());
        } else if (literal.getDatatype() != null) {
            return new LIT(literal.stringValue(), getUID(literal.getDatatype()));
        } else {
            return new LIT(literal.stringValue());
        }
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
        } else {
            return vf.createLiteral(lit.getValue(), getURI(lit.getDatatype()));
        }
    }

    @Override
    public Literal getLiteral(String value) {
//        return vf.createLiteral(value, getURI(XSD.stringType));
        return vf.createLiteral(value);
    }

    @Override
    public Literal getLiteral(String value, Locale locale) {
        if (locale.equals(Locale.ROOT)) {
            return vf.createLiteral(value);
        } else {
            return vf.createLiteral(value, LocaleUtil.toLang(locale));
        }
    }

    @Override
    public Literal getLiteral(String value, URI datatype) {
        return vf.createLiteral(value, datatype);
    }

    @Override
    public NODE getNode(Value node) {
        if (node instanceof Resource) {
            return getID((Resource) node);
        } else {
            return getLIT((Literal) node);
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
    public UID getUID(URI resource) {
        return new UID(resource.getNamespace(), resource.getLocalName());
    }

    @Override
    public URI getURI(UID uid) {
        return vf.createURI(uid.ns(), uid.ln());
    }

    @Override
    public URI getURI(String uri) {
        return vf.createURI(uri);
    }

}
