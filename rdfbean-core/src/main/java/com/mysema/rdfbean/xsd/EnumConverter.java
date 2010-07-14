/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * EnumConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class EnumConverter<T extends Enum<T>> extends AbstractConverter<T>{
    
    private Class<T> enumType;
    
    public EnumConverter(Class<T> enumType){
        this.enumType = enumType;
    }

    @Override
    public T fromString(String str) {
        return Enum.valueOf(enumType, str);
    }

    @Override
    public Class<T> getJavaType() {
        return enumType;
    }

    @Override
    public UID getType() {
        return XSD.stringType;
    }

}
