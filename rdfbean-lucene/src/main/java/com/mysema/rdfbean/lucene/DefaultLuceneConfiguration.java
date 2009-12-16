/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.compass.core.mapping.rsem.builder.RSEM.id;
import static org.compass.core.mapping.rsem.builder.RSEM.property;
import static org.compass.core.mapping.rsem.builder.RSEM.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.compass.core.Compass;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.lucene.internal.MappedClassTypeMapping;
import com.mysema.rdfbean.lucene.internal.TypeMapping;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.owl.OWL;


/**
 * DefaultLuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultLuceneConfiguration implements LuceneConfiguration {

    private boolean indexSupertypes = true;
    
    private Compass compass;
    
    private CompassConfiguration compassConfig;
    
    private boolean contextsStored = false;
    
    private NodeConverter converter;
    
    private Configuration coreConfig;
    
    private PropertyConfig defaultPropertyConfig = null;
    
    private boolean online = true;
    
    private final Map<String,String> prefixToNs = new HashMap<String,String>();
    
    private PropertyConfig rdfTypeConfig = new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, false, false, 1.0f);

    private boolean localNameAsText = true;
    
    private boolean embeddedIds = false;
    
    private TypeMapping typeMapping;
    
    public DefaultLuceneConfiguration(){}
    
    public DefaultLuceneConfiguration(CompassConfiguration compassConfig, Configuration coreConfig){
        this.compassConfig = Assert.notNull(compassConfig);
        this.coreConfig = Assert.notNull(coreConfig);
    }

    public LuceneConfiguration addPrefix(String prefix, String ns){
        prefixToNs.put(prefix, ns);
        return this;
    }

    public Compass getCompass() {
        return compass;
    }

    public Set<UID> getComponentProperties(){
        return typeMapping.getComponentProperties();
    }
    
    public Set<ID> getComponentTypes(){
        return typeMapping.getComponentTypes();
    }

    public NodeConverter getConverter() {
        return converter;
    }
    
    public Configuration getCoreConfiguration() {
        return coreConfig;
    }

    public PropertyConfig getPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes){
        PropertyConfig config = typeMapping.findPropertyConfig(predicate, subjectTypes);
        if (config != null){
            return config;
        }else if (predicate.equals(RDF.type)){
            return rdfTypeConfig;
        }else{
            return defaultPropertyConfig;    
        }              
    }

    public void initialize() {
        Assert.notNull(coreConfig, "coreConfig is null");
        Assert.notNull(compassConfig, "compassConfig is null");
        
        Set<UID> uids = new HashSet<UID>();
        typeMapping = new MappedClassTypeMapping(coreConfig);
        typeMapping.initialize(uids);
                
        uids.addAll(OWL.all);
        uids.addAll(RDF.all);
        uids.addAll(RDFS.all);        
        uids.addAll(XSD.all);
        
        prefixToNs.put("owl", OWL.NS);
        prefixToNs.put("rdf", RDF.NS);
        prefixToNs.put("rdfs", RDFS.NS);
        prefixToNs.put("xsd", XSD.NS);
        converter = new NodeConverter(uids, prefixToNs);
        
        if (online){
            // mapping for general resources        
            compassConfig.addMapping(
                resource("resource")
                    // if field
                    .add(id(Constants.ID_FIELD_NAME))
                    // indexed embedded id field
                    .add(property(Constants.EMBEDDED_ID_FIELD_NAME).store(Store.NO).index(Index.NOT_ANALYZED))
                    // indexed context data
                    .add(property(Constants.CONTEXT_FIELD_NAME).store(Store.NO).index(Index.NOT_ANALYZED))
                    // indexed all field for Object data
                    .add(property(Constants.ALL_FIELD_NAME).store(Store.NO).index(Index.NOT_ANALYZED))
                    // indexed full text for String literals
                    .add(property(Constants.TEXT_FIELD_NAME).store(Store.NO).index(Index.ANALYZED)));
            
            compass = compassConfig.buildCompass();            
        }
        
    }


    public boolean isContextsStored() {
        return contextsStored;
    }
    
    public boolean isOnline() {
        return online;
    }

    public boolean isEmbeddedIds() {
        return embeddedIds;
    }

    public void setCompassConfig(CompassConfiguration compassConfig) {
        this.compassConfig = compassConfig;
    }

    public void setContextsStored(boolean contextsStored) {
        this.contextsStored = contextsStored;
    }

    public void setConverter(NodeConverter converter) {
        this.converter = converter;
    }

    public void setCoreConfiguration(Configuration coreConfig) {
        this.coreConfig = coreConfig;
    }

    public void setDefaultPropertyConfig(PropertyConfig defaultConfig) {
        this.defaultPropertyConfig = defaultConfig;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setRdfTypeConfig(PropertyConfig rdfTypeConfig) {
        this.rdfTypeConfig = rdfTypeConfig;
    }

    public void seEmbeddedIds(boolean useEmbeddedIds) {
        this.embeddedIds = useEmbeddedIds;
    }

    public boolean isLocalNameAsText() {
        return localNameAsText;
    }

    public void setLocalNameAsText(boolean localNameAsText) {
        this.localNameAsText = localNameAsText;
    }

    @Override
    public Collection<? extends ID> getSupertypes(ID type) {
        return typeMapping.getSupertypes(type);
    }
    
    @Override
    public Collection<? extends ID> getSubtypes(ID type) {
        return typeMapping.getSubtypes(type);
    }

    public boolean isIndexSupertypes() {
        return indexSupertypes;
    }

    public void setIndexSupertypes(boolean indexSupertypes) {
        this.indexSupertypes = indexSupertypes;
    }    
    
}
