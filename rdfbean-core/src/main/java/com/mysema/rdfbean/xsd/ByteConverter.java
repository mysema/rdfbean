/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


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

}
