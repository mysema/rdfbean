/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.converters.AbstractConverter;

/**
 * @author tiwe
 */
public class YearConverter extends AbstractConverter<Year> {

    @Override
    public Year fromString(String str) {
        return new Year(str);
    }

    @Override
    public Class<Year> getJavaType() {
        return Year.class;
    }

}
