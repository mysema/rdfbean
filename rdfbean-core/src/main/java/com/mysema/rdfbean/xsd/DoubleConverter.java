/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * DoubleConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DoubleConverter extends AbstractConverter<Double> {

    @Override
    public Double fromString(String str) {
        return Double.valueOf(str);
    }

}
