/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.compass.core.mapping.rsem.builder.RSEM.id;
import static org.compass.core.mapping.rsem.builder.RSEM.property;
import static org.compass.core.mapping.rsem.builder.RSEM.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.compass.core.Compass;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.util.Assert;

import com.mysema.rdfbean.model.ID;
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
import com.mysema.util.ListMap;


/**
 * DefaultLuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
// TODO : clean this up!
public class DefaultLuceneConfiguration implements LuceneConfiguration {

    private Compass compass;
    
    private CompassConfiguration compassConfig;
    
    private final Set<UID> componentProperties = new HashSet<UID>();
    
    private final Set<ID> componentTypes = new HashSet<ID>();
    
    private boolean contextsStored = false;
    
    private NodeConverter converter;
    
    private Configuration coreConfig;
    
    private PropertyConfig defaultPropertyConfig = null;
    
    private RepositoryMode mode = RepositoryMode.INDEX;
    
    private final Map<String,String> prefixToNs = new HashMap<String,String>();
    
    private final Map<ID, Map<UID,PropertyConfig>> propertyConfigs = new HashMap<ID, Map<UID,PropertyConfig>>();
    
    private PropertyConfig rdfTypeConfig = new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, false, 1.0f);

    // TODO : supertypes should be queried from Ontology and not from mapped classes
    private final ListMap<ID,ID> supertypes = new ListMap<ID,ID>();

    public LuceneConfiguration addPrefix(String prefix, String ns){
        prefixToNs.put(prefix, ns);
        return this;
    }

    public Compass getCompass() {
        return compass;
    }

    public Set<UID> getComponentProperties(){
        return componentProperties;
    }
    
    public Set<ID> getComponentTypes(){
        return componentTypes;
    }

    public NodeConverter getConverter() {
        return converter;
    }
    
    public Configuration getCoreConfiguration() {
        return coreConfig;
    }

    public RepositoryMode getMode() {
        return mode;
    }

    public PropertyConfig getPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes){
        while (!subjectTypes.isEmpty()){
            for (ID type : subjectTypes){
                Map<UID,PropertyConfig> configs = propertyConfigs.get(type);                
                if (configs != null && configs.containsKey(predicate)){
                    return configs.get(predicate);
                }
            }    
            List<ID> newTypes = new ArrayList<ID>(subjectTypes.size());
            for (ID type : subjectTypes){
                if (supertypes.containsKey(type)){
                    newTypes.addAll(supertypes.get(type));
                }
            }
            subjectTypes = newTypes;
        }
        if (predicate.equals(RDF.type)){
            return rdfTypeConfig;
        }else{
            return defaultPropertyConfig;    
        }              
    }

    public void initialize() {
        Assert.notNull(coreConfig, "coreConfig is null");
        Assert.notNull(compassConfig, "compassConfig is null");
        
        Set<UID> uids = new HashSet<UID>();
        initializePropertyConfigs(uids);
        
        uids.addAll(OWL.all);
        uids.addAll(RDF.all);
        uids.addAll(RDFS.all);        
        uids.addAll(XSD.all);
        
        prefixToNs.put("owl", OWL.NS);
        prefixToNs.put("rdf", RDF.NS);
        prefixToNs.put("rdfs", RDFS.NS);
        prefixToNs.put("xsd", XSD.NS);
        converter = new NodeConverter(uids, prefixToNs);
        
        if (mode != RepositoryMode.OFFLINE){
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
            
            compass = compassConfig.buildCompass();            
        }
        
    }

    private void initializePropertyConfigs(Collection<UID> uids) {
        // handle types
        for (Class<?> javaClass : coreConfig.getMappedClasses()){
            MappedClass clazz = MappedClass.getMappedClass(javaClass);
            uids.add(clazz.getUID());
            
         // register supertypes
            for (MappedClass superClass : clazz.getMappedSuperClasses()){
                supertypes.put(clazz.getUID(), superClass.getUID());
            }
            
            Searchable searchable = clazz.getAnnotation(Searchable.class);
            if (searchable != null){
                if (searchable.embeddedOnly()){
                    componentTypes.add(clazz.getUID());
                }                
                Map<UID,PropertyConfig> configs = new HashMap<UID,PropertyConfig>();
                
                // handle properties
                for (MappedPath mappedPath : clazz.getProperties()){
                    MappedProperty<?> property = mappedPath.getMappedProperty();
                    
                    // predicate configuration
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
                        for (MappedPredicate pred : mappedPath.getPredicatePath()){
                            uids.add(pred.getUID());
                        }
                        // TODO : handle longer predicate paths
                        MappedPredicate predicate = mappedPath.getPredicatePath().get(0);
                        boolean textIndexed = searchablePred != null ? searchablePred.text() : false;
                        boolean allIndexed = searchablePred != null ? searchablePred.all() : false;      
                        float boost = searchablePred != null ? searchablePred.boost() : 1.0f;
                        PropertyConfig propertyConfig = new PropertyConfig(store, index, textIndexed, allIndexed, boost);
                        configs.put(predicate.getUID(), propertyConfig);
                    }      
                    
                    // component configuration
                    SearchableComponent searchableComp = property.getAnnotation(SearchableComponent.class);
                    if (searchableComp != null){
                        // TODO : handle longer predicate paths
                        MappedPredicate predicate = mappedPath.getPredicatePath().get(0);
                        componentProperties.add(predicate.getUID());
                    }
                    
                }
                propertyConfigs.put(clazz.getUID(), configs);
            }
        }
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

    public void setDefaultPropertyConfig(PropertyConfig defaultConfig) {
        this.defaultPropertyConfig = defaultConfig;
    }

    public void setMode(RepositoryMode mode) {
        this.mode = mode;
    }

    public void setRdfTypeConfig(PropertyConfig rdfTypeConfig) {
        this.rdfTypeConfig = rdfTypeConfig;
    }
    
    
}
