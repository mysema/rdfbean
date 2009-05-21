/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


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

}
