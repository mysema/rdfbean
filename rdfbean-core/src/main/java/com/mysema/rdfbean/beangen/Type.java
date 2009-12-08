/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * Type provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class Type {
    
    private final UID rdfType;
    
    private final String packageName;
    
    private final String simpleName;
    
    public Type(UID rdfType, String packageName, String simpleName){
        this.rdfType = rdfType;
        this.packageName = packageName;
        this.simpleName = simpleName;
    }
    
    public Type(UID rdfType, Class<?> clazz) {
        this(rdfType, clazz.getPackage().getName(), clazz.getSimpleName());
    }

    public UID getRdfType() {
        return rdfType;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getLocalName(){
        return simpleName;
    }
    
    @Override
    public String toString(){
        return getPackageName() + "." + getLocalName();
    }
    
    @Override
    public int hashCode(){
        return simpleName.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if (o instanceof Type){
            return ((Type)o).packageName.equals(packageName) 
                && ((Type)o).simpleName.equals(simpleName);
        }else{
            return false;
        }
    }
}
