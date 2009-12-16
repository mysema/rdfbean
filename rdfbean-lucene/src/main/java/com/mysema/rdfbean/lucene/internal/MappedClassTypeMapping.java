/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
import com.mysema.util.Pair;
import com.mysema.util.SetMap;

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
    
    private final Map<Pair<ID,UID>,PropertyConfig> propertyConfigs = new HashMap<Pair<ID,UID>,PropertyConfig>();
    
    private final Set<ID> types = new HashSet<ID>();
    
    private final SetMap<ID,ID> directSubtypes = new SetMap<ID,ID>();
    
    private final SetMap<ID,ID> directSupertypes = new SetMap<ID,ID>();
    
    private final SetMap<ID,ID> subtypes = new SetMap<ID,ID>();
    
    private final SetMap<ID,ID> supertypes = new SetMap<ID,ID>();    
    
    public MappedClassTypeMapping(Configuration coreConfig){
        this.coreConfig = coreConfig;
    }

    public PropertyConfig findPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes) {
        for (ID type : subjectTypes){
            PropertyConfig config = propertyConfigs.get(new Pair<ID,UID>(type, predicate));
            if (config != null){
                return config;
            }
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
        Map<ID, Map<UID,PropertyConfig>> typeToConfigs = new HashMap<ID, Map<UID,PropertyConfig>>();
        
        // handle types
        for (Class<?> javaClass : coreConfig.getMappedClasses()){
            MappedClass clazz = MappedClass.getMappedClass(javaClass);
            types.add(clazz.getUID());
            uids.add(clazz.getUID());
            
            for (MappedClass superClass : clazz.getMappedSuperClasses()){
                directSupertypes.put(clazz.getUID(), superClass.getUID());
                directSubtypes.put(superClass.getUID(), clazz.getUID());
            }
            
            Searchable searchable = clazz.getAnnotation(Searchable.class);
            if (searchable != null){
                if (searchable.embeddedOnly()){
                    componentTypes.add(clazz.getUID());
                }                
                Map<UID,PropertyConfig> configs = new HashMap<UID,PropertyConfig>();                
                // handle properties
                for (MappedPath mappedPath : clazz.getProperties()){
                    initializeProperty(mappedPath, uids, searchable, configs);                    
                }
                typeToConfigs.put(clazz.getUID(), configs);
            }
        }
        
        // initialize subtype/supertype mappings               
        initializeTypeHierarchy();
        
        // flatten typeconfig structure
        for (ID type : types){
            Map<UID,PropertyConfig> configs = typeToConfigs.get(type);
            if (configs != null && !configs.isEmpty()){
                for (Map.Entry<UID, PropertyConfig> entry : configs.entrySet()){
                    for (ID subtype : subtypes.get(type)){
                        propertyConfigs.put(new Pair<ID,UID>(subtype, entry.getKey()), entry.getValue());
                    }
                }
            }
        }
    }
    
    private void initializeTypeHierarchy(){
        // populate mappings for transitive super and subtypes
        for (ID type : types){
            subtypes.put(type, type);
            supertypes.put(type, type);
            
            // handle subtypes
            if (directSubtypes.containsKey(type)){
                Stack<ID> t = new Stack<ID>();
                t.addAll(directSubtypes.get(type));
                while (!t.isEmpty()){
                    ID subtype = t.pop();
                    subtypes.put(type, subtype);
                    if (directSubtypes.containsKey(subtype)){
                        t.addAll(directSubtypes.get(subtype));
                    }
                }
            }
            
            // handle supertypes
            if (directSupertypes.containsKey(type)){
                Stack<ID> t = new Stack<ID>();
                t.addAll(directSupertypes.get(type));
                while (!t.isEmpty()){
                    ID subtype = t.pop();
                    supertypes.put(type, subtype);
                    if (directSupertypes.containsKey(subtype)){
                        t.addAll(directSupertypes.get(subtype));
                    }
                }
            }
        }
    }

    private void initializeProperty(MappedPath mappedPath, Collection<UID> uids, Searchable searchable, Map<UID, PropertyConfig> configs) {
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
