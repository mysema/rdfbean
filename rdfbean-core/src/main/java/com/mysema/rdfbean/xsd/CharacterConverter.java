/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * CharacterConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CharacterConverter extends AbstractConverter<Character> {

    @Override
    public Character fromString(String str) {
        return Character.valueOf(str.charAt(0));
    }

    @Override
    public Class<Character> getJavaType() {
        return Character.class;
    }

    @Override
    public UID getType() {
        return XSD.stringType;
    }

}
