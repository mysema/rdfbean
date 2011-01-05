/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;



/**
 * BooleanConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BooleanConverter extends AbstractConverter<Boolean> {

    @Override
    public Boolean fromString(String str) {
        return Boolean.valueOf(str);
    }

    @Override
    public Class<Boolean> getJavaType() {
        return Boolean.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.booleanType;
//    }

}
