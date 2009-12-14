/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.util.Map;

import com.mysema.rdfbean.model.UID;

/**
 * MapType provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MapType extends Type{

    private final Type keyType, valueType;
    
    public MapType(UID rdfType, Type keyType, Type valueType) {
        super(rdfType, Map.class);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

    @Override
    public String getLocalName(){
        return getSimpleName() + "<" + keyType + "," + valueType + ">";
    }
    
}
