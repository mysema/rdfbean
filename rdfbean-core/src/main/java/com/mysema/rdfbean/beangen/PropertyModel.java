/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * PropertyModel provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class PropertyModel implements Comparable<PropertyModel>{

    private final UID rdfProperty;
    
    private final String name;
    
    private final TypeModel type;
    
    @Nullable
    private final TypeModel keyType, valueType;
    
    public PropertyModel(UID rdfProperty, String name, TypeModel type){
        this(rdfProperty, name, type, null, null);
    }
    
    public PropertyModel(UID rdfProperty, String name, TypeModel type, 
            @Nullable TypeModel keyType, @Nullable TypeModel valueType){
        this.rdfProperty = rdfProperty;
        this.name = name;
        this.type = type;
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    public UID getRdfProperty() {
        return rdfProperty;
    }

    public String getName() {
        return name;
    }

    public TypeModel getType() {
        return type;
    }
    
    
    
    public TypeModel getKeyType() {
        return keyType;
    }

    public TypeModel getValueType() {
        return valueType;
    }

    @Override
    public String toString(){
        return type + " " + name;
    }

    @Override
    public int compareTo(PropertyModel o) {
        return name.compareTo(o.name);
    }

    public PropertyModel merge(PropertyModel other, TypeModel defaultType) {
        if (!other.getType().equals(defaultType)){
            return other;
        }else{
            return this;
        }        
    }
    
}
