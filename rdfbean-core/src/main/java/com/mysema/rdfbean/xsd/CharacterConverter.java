/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

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

}
