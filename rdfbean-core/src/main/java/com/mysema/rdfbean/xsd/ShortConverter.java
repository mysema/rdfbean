/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


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

}
