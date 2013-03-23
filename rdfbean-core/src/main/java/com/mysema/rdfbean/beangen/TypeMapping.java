/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.primitives.Primitives;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.Year;

/**
 * @author tiwe
 */
public class TypeMapping {

    private static final Type YEAR = new ClassType(TypeCategory.COMPARABLE, Year.class);

    private final Map<UID, Type> datatypeToType = new HashMap<UID, Type>();

    private final boolean usePrimitives;

    private Type defaultType;

    public TypeMapping(boolean usePrimitives) {
        this.usePrimitives = usePrimitives;
        register(RDF.text, Types.STRING);
        register(XSD.anyURI, Types.URI);
        register(XSD.booleanType, Types.BOOLEAN);
        register(XSD.byteType, Types.BYTE);
        register(XSD.decimalType, Types.BIG_DECIMAL);
        register(XSD.doubleType, Types.DOUBLE);
        // duration
        register(XSD.floatType, Types.FLOAT);
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, YEAR);
        // gYearMonth
        register(XSD.integerType, Types.BIG_INTEGER);
        register(XSD.intType, Types.INT);
        register(XSD.longType, Types.LONG);
        register(XSD.shortType, Types.SHORT);
        register(XSD.stringType, Types.STRING);
        register(RDFS.Literal, Types.STRING);

        register(XSD.date, Types.LOCAL_DATE); // joda-time
        register(XSD.dateTime, Types.DATE_TIME); // joda-time
        register(XSD.time, Types.LOCAL_TIME); // joda-time

        defaultType = datatypeToType.get(XSD.stringType);
    }

    private void register(UID uid, Type type) {
        if (usePrimitives && Primitives.isWrapperType(type.getJavaClass())) {
            String name = Primitives.unwrap(type.getJavaClass()).getName();
            type = new SimpleType(type.getCategory(), "java.lang." + name, "java.lang", name, true, true);
        }
        datatypeToType.put(uid, type);
    }

    public Type getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(Type type) {
        this.defaultType = type;
    }

    public boolean containsKey(ID id) {
        return datatypeToType.containsKey(id);
    }

    public Type get(ID id) {
        return datatypeToType.get(id);
    }

}
