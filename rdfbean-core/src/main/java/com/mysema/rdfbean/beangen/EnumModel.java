/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.util.ArrayList;
import java.util.List;

import com.mysema.rdfbean.model.UID;

/**
 * EnumModel provides
 *
 * @author tiwe
 * @version $Id$
 */
public class EnumModel extends TypeModel{
    
    private final List<String> enums = new ArrayList<String>();
    
    public EnumModel(UID rdfType, String packageName, String simpleName){
        super(rdfType, packageName, simpleName);
    }
    
    public EnumModel addEnum(String name){
        enums.add(name);
        return this;
    }
    
    public List<String> getEnums() {
        return enums;
    }
    
}
