/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * Property provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class Property implements Comparable<Property>{

    private final UID rdfProperty;
    
    private final String name;
    
    private boolean multipleValues;
    
    private Type type;
    
    public Property(UID rdfProperty, String name, Type type){
        this.rdfProperty = rdfProperty;
        this.name = name;
        this.type = type;
    }
    
    public UID getRdfProperty() {
        return rdfProperty;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
    
    public void setType(Type type){
        this.type = type;
    }

    @Override
    public String toString(){
        return type + " " + name;
    }

    @Override
    public int compareTo(Property o) {
        return name.compareTo(o.name);
    }
    
    public boolean isMultipleValues() {
        return multipleValues;
    }

    public void setMultipleValues(boolean b) {
        this.multipleValues = b;
        
    }
    
}
