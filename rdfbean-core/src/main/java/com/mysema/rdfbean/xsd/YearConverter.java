/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * YearConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class YearConverter  extends AbstractConverter<Year> {

    @Override
    public Year fromString(String str) {
        return new Year(str);
    }

    @Override
    public Class<Year> getJavaType() {
        return Year.class;
    }

    @Override
    public UID getType() {
        return XSD.gYear;
    }

}
