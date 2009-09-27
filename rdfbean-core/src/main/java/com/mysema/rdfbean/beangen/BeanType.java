/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.mysema.rdfbean.model.UID;

/**
 * BeanModel provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanType extends Type{
    
    private final Set<Property> properties = new TreeSet<Property>();

    private final List<Type> superTypes = new ArrayList<Type>();
    
    public BeanType(UID rdfType, String packageName, String simpleName){
        super(rdfType, packageName, simpleName);
    }

    public Set<Property> getProperties() {
        return properties;
    }
    
    public void addProperty(Property property){
        properties.add(property);
    }
    
    public List<Type> getSuperTypes() {
        return superTypes;
    }

    public void addSuperType(Type type) {
        superTypes.add(type);
        
    }

}
