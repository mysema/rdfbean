/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.query.codegen.ClassType;
import com.mysema.query.codegen.SimpleType;
import com.mysema.query.codegen.Type;
import com.mysema.query.codegen.TypeCategory;
import com.mysema.query.codegen.Types;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.Year;

/**
 * TypeMapping provides
 *
 * @author tiwe
 * @version $Id$
 */
public class TypeMapping {
    
    private static final Type LOCAL_DATE = new ClassType(TypeCategory.DATE, LocalDate.class);
    
    private static final Type DATE_TIME = new ClassType(TypeCategory.DATETIME, DateTime.class);
    
    private static final Type LOCAL_TIME = new ClassType(TypeCategory.TIME, LocalTime.class);
    
    // TODO : move to Types
    private static final Type BIG_DECIMAL = new ClassType(TypeCategory.NUMERIC, BigDecimal.class);

    // TODO : move to Types
    private static final Type BIG_INTEGER = new ClassType(TypeCategory.NUMERIC, BigInteger.class);
    
    // TODO : move to Types
    private static final Type URI = new ClassType(TypeCategory.COMPARABLE, URI.class);
    
    private static final Type YEAR = new ClassType(TypeCategory.COMPARABLE, Year.class);
    
    private final Map<UID,Type> datatypeToType = new HashMap<UID,Type>();
    
    private final boolean usePrimitives;
    
    private Type defaultType;
    
    public TypeMapping(boolean usePrimitives){
        this.usePrimitives = usePrimitives;
        register(RDF.text, Types.STRING);
        register(XSD.anyURI, URI);
        register(XSD.booleanType, Types.BOOLEAN);
        register(XSD.byteType, Types.BYTE);               
        register(XSD.decimalType, BIG_DECIMAL);
        register(XSD.doubleType, Types.DOUBLE);
        // duration
        register(XSD.floatType, Types.FLOAT);
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, YEAR);
        // gYearMonth
        register(XSD.integerType, BIG_INTEGER);
        register(XSD.intType, Types.INT);
        register(XSD.longType, Types.LONG);
        register(XSD.shortType, Types.SHORT);
        register(XSD.stringType, Types.STRING);               
        register(RDFS.Literal, Types.STRING);
        
        register(XSD.date, LOCAL_DATE); // joda-time
        register(XSD.dateTime, DATE_TIME); // joda-time
        register(XSD.time, LOCAL_TIME); // joda-time
        
        defaultType = datatypeToType.get(XSD.stringType);
    }
    
    private void register(UID uid, Type type) {
        if (usePrimitives && type.getPrimitiveName() != null){
            String name = type.getPrimitiveName();
            type = new SimpleType(type.getCategory(),"java.lang."+name, "java.lang", name,true);
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
