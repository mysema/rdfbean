/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * ShortConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ShortConverter  extends AbstractConverter<Short> {

    @Override
    public Short fromString(String str) {
        return Short.valueOf(str);
    }

    @Override
    public Class<Short> getJavaType() {
        return Short.class;
    }

    @Override
    public UID getType() {
        return XSD.shortType;
    }

}
