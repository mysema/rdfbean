/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.compass.core.mapping.rsem.builder.RSEM.id;
import static org.compass.core.mapping.rsem.builder.RSEM.property;
import static org.compass.core.mapping.rsem.builder.RSEM.resource;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;


/**
 * LuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConfiguration {

    private NodeConverter converter = NodeConverter.DEFAULT;
    
    private CompassConfiguration compassConfig;
    
    private boolean allIndexed = true;
    
    private boolean fullTextIndexed = true;
    
    private boolean stored = true;
    
    private boolean contextsStored = true;
    
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

    public void initialize() {
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
