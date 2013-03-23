/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * @author tiwe
 * 
 */
final class Constants {

    public static final Set<UID> decimalTypes = asSet(XSD.decimalType, XSD.doubleType, XSD.floatType);

    public static final Set<UID> integerTypes = asSet(XSD.integerType, XSD.longType, XSD.intType, XSD.shortType, XSD.byteType);

    public static final Set<UID> dateTypes = Collections.singleton(XSD.date);

    public static final Set<UID> dateTimeTypes = Collections.singleton(XSD.dateTime);

    private Constants() {
    }

    private static <T> Set<T> asSet(T... args) {
        return new HashSet<T>(Arrays.asList(args));
    }

}
