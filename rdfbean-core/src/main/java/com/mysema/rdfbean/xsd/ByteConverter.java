/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * ByteConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ByteConverter extends AbstractConverter<Byte> {

    @Override
    public Byte fromString(String str) {
        return Byte.valueOf(str);
    }

    @Override
    public Class<Byte> getJavaType() {
        return Byte.class;
    }

    @Override
    public UID getType() {
        return XSD.byteType;
    }

}
