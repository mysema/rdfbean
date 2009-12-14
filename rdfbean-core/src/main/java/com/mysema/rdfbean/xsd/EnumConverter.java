/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

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

}
