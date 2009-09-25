/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * TypeModel provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class TypeModel {
    
    private final UID rdfType;
    
    private final String packageName;
    
    private final String simpleName;
    
    public TypeModel(UID rdfType, String packageName, String simpleName){
        this.rdfType = rdfType;
        this.packageName = packageName;
        this.simpleName = simpleName;
    }
    
    public TypeModel(UID rdfType, Class<?> clazz) {
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

    public String toString(){
        return packageName + "." + simpleName;
    }
    
    public int hashCode(){
        return simpleName.hashCode();
    }
    
    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if (o instanceof TypeModel){
            return ((TypeModel)o).packageName.equals(packageName) 
                && ((TypeModel)o).simpleName.equals(simpleName);
        }else{
            return false;
        }
    }
}
