/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;

import com.mysema.rdfbean.lucene.PropertyConfig;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchableComponent;
import com.mysema.rdfbean.lucene.SearchablePredicate;
import com.mysema.rdfbean.lucene.SearchableText;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.util.ListMap;

/**
 * MappedClassTypeMapping extracts mapping information from MappedClass instances 
 *
 * @author tiwe
 * @version $Id$
 */
public class MappedClassTypeMapping implements TypeMapping {
    
    private final Set<UID> componentProperties = new HashSet<UID>();
    
    private final Set<ID> componentTypes = new HashSet<ID>();
    
    private final Configuration coreConfig;
    
    private final Map<ID, Map<UID,PropertyConfig>> propertyConfigs = new HashMap<ID, Map<UID,PropertyConfig>>();
    
    private final ListMap<ID,ID> subtypes = new ListMap<ID,ID>();
    
    private final ListMap<ID,ID> supertypes = new ListMap<ID,ID>();
    
    public MappedClassTypeMapping(Configuration coreConfig){
        this.coreConfig = coreConfig;
    }

    public PropertyConfig findPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes) {
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
        return null;
    }

    public Set<UID> getComponentProperties() {
        return componentProperties;
    }

    public Set<ID> getComponentTypes() {
        return componentTypes;
    }

    public Collection<? extends ID> getSubtypes(ID type) {
        Collection<ID> rv = subtypes.get(type);
        return rv != null ? rv : Collections.<ID>emptySet();
    }

    public Collection<? extends ID> getSupertypes(ID type) {
        Collection<ID> rv = supertypes.get(type);
        return rv != null ? rv : Collections.<ID>emptySet();
    }

    public void initialize(Collection<UID> uids) {
        // handle types
        for (Class<?> javaClass : coreConfig.getMappedClasses()){
            MappedClass clazz = MappedClass.getMappedClass(javaClass);
            uids.add(clazz.getUID());
            
            for (MappedClass superClass : clazz.getMappedSuperClasses()){
                supertypes.put(clazz.getUID(), superClass.getUID());
                subtypes.put(superClass.getUID(), clazz.getUID());
            }
            
            Searchable searchable = clazz.getAnnotation(Searchable.class);
            if (searchable != null){
                if (searchable.embeddedOnly()){
                    componentTypes.add(clazz.getUID());
                }                
                Map<UID,PropertyConfig> configs = new HashMap<UID,PropertyConfig>();                
                // handle properties
                for (MappedPath mappedPath : clazz.getProperties()){
                    initializeProperty(uids, searchable, configs, mappedPath);                    
                }
                propertyConfigs.put(clazz.getUID(), configs);
            }
        }
        
        // TODO : populate transitive type relations
    }

    private void initializeProperty(Collection<UID> uids, Searchable searchable,
            Map<UID, PropertyConfig> configs, MappedPath mappedPath) {
        MappedProperty<?> property = mappedPath.getMappedProperty();
        
        // predicate configuration
        SearchablePredicate searchablePred = property.getAnnotation(SearchablePredicate.class);
        boolean textIndexed = property.getAnnotation(SearchableText.class) != null;
        Index index = searchablePred != null ? searchablePred.index() : null;
        Store store = null;
        if (searchable.storeAll()){
            store = Store.YES;
        }else if (searchablePred != null){
            store = searchablePred.store();
        }
        if (index != null || store != null || textIndexed){
            if (index == null) index = Index.NO;
            for (MappedPredicate pred : mappedPath.getPredicatePath()){
                uids.add(pred.getUID());
            }
            // TODO : handle longer predicate paths
            MappedPredicate predicate = mappedPath.getPredicatePath().get(0);
            boolean inverted = predicate.inv();
            boolean allIndexed = searchablePred != null ? searchablePred.all() : false;      
            float boost = searchablePred != null ? searchablePred.boost() : 1.0f;
            PropertyConfig propertyConfig = new PropertyConfig(store, index, textIndexed, allIndexed, inverted, boost);
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

}
