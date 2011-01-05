/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;



/**
 * LongConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LongConverter extends AbstractConverter<Long> {

    @Override
    public Long fromString(String str) {
        return Long.valueOf(str);
    }

    @Override
    public Class<Long> getJavaType() {
        return Long.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.longType;
//    }

}
