/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.*;

/**
 * MulgaraDialect provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraDialect extends Dialect<Node, SubjectNode, BlankNode, URIReference, Literal, Triple> {

    private static final Logger logger = LoggerFactory.getLogger(MulgaraDialect.class);
    
    private static final Map<String,URI> datatypeURICache = new HashMap<String,URI>();
    
    static{
        for (UID uid : XSD.ALL){
            datatypeURICache.put(uid.getId(), URI.create(uid.getId()));    
        }      
    }
    
    private final Map<BlankNode, BID> bnodeCache = new HashMap<BlankNode, BID>(1024);
    
    private final GraphElementFactory elementFactory;
    
    private final Map<Literal, LIT> literalCache = new HashMap<Literal, LIT>(1024); 
    
    private final Map<URIReference, UID> uriCache = new HashMap<URIReference, UID>(1024); 
    
    public MulgaraDialect(GraphElementFactory elementFactory){
        this.elementFactory = Assert.notNull(elementFactory,"elementFactory");
    }
    
    @Override
    public BlankNode createBNode() {
        try {
            return elementFactory.createResource();
        } catch (GraphElementFactoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
    }
    
    @Override
    public Triple createStatement(SubjectNode subject, URIReference predicate,
            Node object) {
        try {
            return elementFactory.createTriple(subject, predicate, (ObjectNode) object);
        } catch (GraphElementFactoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
    }

    @Override
    public Triple createStatement(SubjectNode subject, URIReference predicate,
            Node object, URIReference context) {
        try {
            return elementFactory.createTriple(subject, predicate, (ObjectNode) object);
        } catch (GraphElementFactoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
    }

    @Override
    public BID getBID(BlankNode bnode) {
        BID bid = bnodeCache.get(bnode);
        if (bid == null) {
            bid = new BID(bnode.getID());
            bnodeCache.put(bnode, bid);
        }
        return bid;
    }

    @Override
    public BlankNode getBNode(BID bid) {
        // TODO : make sure this works!
        return new BlankNodeImpl(Long.parseLong(bid.getValue()));
    }

    protected URI getDatatypeURI(String datatype) {
        URI uri = datatypeURICache.get(datatype);
        if (uri == null){
            uri = URI.create(datatype);
            datatypeURICache.put(datatype, uri);
        }
        return uri;        
    }


    @Override
    public ID getID(SubjectNode resource) {
        if (resource instanceof URIReference){
            return getUID((URIReference) resource);
        }else if (resource instanceof BlankNode){
            return getBID((BlankNode) resource);
        }else{
            throw new IllegalArgumentException("Expected URIReference or BlankNode, got " + resource);
        }
    }

    @Override
    public LIT getLIT(Literal literal) {
        LIT lit = literalCache.get(literal);
        if (lit == null) {
            if (literal.getLanguage() != null){
                lit = new LIT(literal.getLabel(), literal.getLanguage());
            }else if (literal.getDatatype() != null){
                UID datatype = getDatatypeUID(literal.getDatatype().stringValue());
                lit = new LIT(literal.getLabel(), datatype);
            }else{
                lit = new LIT(literal.getLabel(), RDF.text);
            }    
            literalCache.put(literal, lit);
        }
        return lit;
        
    }

    @Override
    public Literal getLiteral(LIT lit) {
        try{
            if (lit.isText()){
                Locale lang = lit.getLang();
                if (lang.equals(Locale.ROOT)){
                    return elementFactory.createLiteral(lit.getValue());
                }else{
                    return elementFactory.createLiteral(lit.getValue(), LocaleUtil.toLang(lang));
                }
            }else if (lit.getDatatype().equals(RDF.text)){
                return elementFactory.createLiteral(lit.getValue());
            }else{
                return elementFactory.createLiteral(lit.getValue(), getDatatypeURI(lit.getDatatype().getId()));
            }
        } catch (GraphElementFactoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }        
    }

    @Override
    public NODE getNODE(Node node) {
        if (node instanceof BlankNode){
            return getBID((BlankNode)node);
        }else if (node instanceof URIReference){
            return getUID((URIReference)node);
        }else if (node instanceof Literal){
            return getLIT((Literal)node);
        }else{
            throw new IllegalArgumentException("Illegal node : " + node);
        }
    }
    
    @Override
    public NodeType getNodeType(Node node) {
        if (node.isBlankNode()){
            return NodeType.BLANK;
        }else if (node.isURIReference()){
            return NodeType.URI;
        }else if (node.isLiteral()){
            return NodeType.LITERAL;
        }else{
            throw new IllegalArgumentException("Illegal node : " + node); 
        }
    }

    @Override
    public Node getObject(Triple statement) {
        return statement.getObject();
    }

    @Override
    public URIReference getPredicate(Triple statement) {
        return (URIReference) statement.getPredicate();
    }

    @Override
    public SubjectNode getSubject(Triple statement) {
        return statement.getSubject();
    }

    @Override
    public UID getUID(URIReference uri) {
        UID uid = uriCache.get(uri);
        if (uid == null) {
            uid = new UID(uri.getNamespace(), uri.getLocalName());
            uriCache.put((URIReference) uri, uid);
        }
        return uid; 
    }

    @Override
    public URIReference getURI(UID uid) {
        try {
            URI uri = URI.create(uid.getValue());
            return elementFactory.createResource(uri);
        } catch (GraphElementFactoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
        
    }

}
