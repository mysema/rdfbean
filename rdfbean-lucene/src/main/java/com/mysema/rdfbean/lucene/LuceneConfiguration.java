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
import org.compass.core.util.Assert;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.owl.OWL;


/**
 * LuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConfiguration {

    private CompassConfiguration compassConfig;
    
    private boolean contextsStored = true;
    
    private NodeConverter converter;
    
    private Configuration coreConfig;
    
    private PropertyConfig rdfTypeConfig = new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, false);
    
    private PropertyConfig defaultPropertyConfig = null;
    
    private final Map<UID, PropertyConfig> propertyConfigs = new HashMap<UID, PropertyConfig>();

    private final Map<String,String> nsToPrefix = new HashMap<String,String>();

    private final Map<String,String> prefixToNs = new HashMap<String,String>();
    
    public LuceneConfiguration(){
        addPrefix("rdf", RDF.NS);
        addPrefix("rdfs", RDFS.NS);
        addPrefix("owl", OWL.NS);
        addPrefix("xsd", XSD.NS);
    }
     
    public void initialize() {
        Assert.notNull(coreConfig, "coreConfig is null");
        Assert.notNull(compassConfig, "compassConfig is null");
        
        initializePropertyConfigs();
        
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
        
    }

    private void initializePropertyConfigs() {
        // rdf:type is always stored
        propertyConfigs.put(RDF.type, rdfTypeConfig);
        
        for (Class<?> javaClass : coreConfig.getMappedClasses()){
            MappedClass clazz = MappedClass.getMappedClass(javaClass);
            Searchable searchable = clazz.getAnnotation(Searchable.class);
            if (searchable != null){
                for (MappedPath mappedPath : clazz.getProperties()){
                    MappedProperty<?> property = mappedPath.getMappedProperty();
                    SearchablePredicate searchablePred = property.getAnnotation(SearchablePredicate.class);
                    Index index = searchablePred != null ? searchablePred.index() : null;
                    Store store = null;
                    if (searchable.storeAll()){
                        store = Store.YES;
                    }else if (searchablePred != null){
                        store = searchablePred.store();
                    }
                    if (index != null || store != null){
                        if (index == null) index = Index.NO;
                        // TODO : handle longer predicate paths
                        MappedPredicate predicate = mappedPath.getPredicatePath().get(0);
                        boolean textIndexed = searchablePred != null ? searchablePred.textIndexed() : false;
                        boolean allIndexed = searchablePred != null ? searchablePred.allIndexed() : false;                        
                        PropertyConfig propertyConfig = new PropertyConfig(store, index, textIndexed, allIndexed);
                        propertyConfigs.put(predicate.getUID(), propertyConfig);
                    }
                    
                }
            }
        }
    }

    
    public LuceneConfiguration addPrefix(String prefix, String ns){
        prefixToNs.put(prefix, ns);
        nsToPrefix.put(ns, prefix);
        return this;
    }

    public PropertyConfig getPropertyConfig(UID predicate){
        if (propertyConfigs.containsKey(predicate)){
            return propertyConfigs.get(predicate);    
        }else{
            return defaultPropertyConfig;
        }        
    }
    
    public CompassConfiguration getCompassConfig() {
        return compassConfig;
    }

    public NodeConverter getConverter() {
        return converter;
    }

    public Configuration getCoreConfiguration() {
        return coreConfig;
    }

    public boolean isContextsStored() {
        return contextsStored;
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

    public void setRdfTypeConfig(PropertyConfig rdfTypeConfig) {
        this.rdfTypeConfig = rdfTypeConfig;
    }

    public void setDefaultPropertyConfig(PropertyConfig defaultConfig) {
        this.defaultPropertyConfig = defaultConfig;
    }

    
}
