/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.compass.core.mapping.rsem.builder.RSEM.id;
import static org.compass.core.mapping.rsem.builder.RSEM.property;
import static org.compass.core.mapping.rsem.builder.RSEM.resource;

import java.util.HashMap;
import java.util.Map;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;


/**
 * LuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConfiguration {

    private final Map<String,String> prefixToNs = new HashMap<String,String>();
    
    private final Map<String,String> nsToPrefix = new HashMap<String,String>();
    
    private NodeConverter converter;
    
    private CompassConfiguration compassConfig;
    
    /**
     * Index all statements into 'all' field
     */
    private boolean allIndexed = true;
    
    /**
     * Index literal statements into 'text' field for full text search
     */
    private boolean fullTextIndexed = true;
    
    /**
     * Store statements into predicate fields
     */
    private boolean stored = true;
    
    /**
     * Store contexts
     */
    private boolean contextsStored = true;
    
    public LuceneConfiguration(){
        addPrefix("rdf", RDF.NS);
        addPrefix("rdfs", RDFS.NS);
        addPrefix("owl", OWL.NS);
        addPrefix("xsd", XSD.NS);
    }
        
    public NodeConverter getConverter() {
        return converter;
    }

    public boolean isStored() {
        return stored;
    }

    public void setConverter(NodeConverter converter) {
        this.converter = converter;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    public boolean isFullTextIndexed() {
        return fullTextIndexed;
    }

    public void setFullTextIndexed(boolean fullTextIndexed) {
        this.fullTextIndexed = fullTextIndexed;
    }

    public boolean isContextsStored() {
        return contextsStored;
    }

    public void setContextsStored(boolean contextsStored) {
        this.contextsStored = contextsStored;
    }

    public CompassConfiguration getCompassConfig() {
        return compassConfig;
    }

    public void setCompassConfig(CompassConfiguration compassConfig) {
        this.compassConfig = compassConfig;
    }

    public boolean isAllIndexed() {
        return allIndexed;
    }

    public void setAllIndexed(boolean allIndexed) {
        this.allIndexed = allIndexed;
    }
    
    public LuceneConfiguration addPrefix(String prefix, String ns){
        prefixToNs.put(prefix, ns);
        nsToPrefix.put(ns, prefix);
        return this;
    }    

    public void initialize() {
        converter = new NodeConverter(prefixToNs, nsToPrefix);
        
        // mapping for general resources        
        compassConfig.addMapping(
            resource("resource")
                // if field
                .add(id(Constants.ID_FIELD_NAME))
                // context data
                .add(property(Constants.CONTEXT_FIELD_NAME).store(Store.YES).index(Index.NOT_ANALYZED))
                // all field for Object data
                .add(property(Constants.ALL_FIELD_NAME).store(Store.NO).index(Index.NOT_ANALYZED))
                // full text for String literals
                .add(property(Constants.TEXT_FIELD_NAME).store(Store.NO).index(Index.ANALYZED)));
        
        // TODO : rdf:type
    }
    
}
