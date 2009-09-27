package com.mysema.rdfbean.beangen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

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
    
    private final Map<UID,Type> datatypeToType = new HashMap<UID,Type>();
    
    private final boolean usePrimitives;
    
    private Type defaultType;
    
    public TypeMapping(boolean usePrimitives){
        this.usePrimitives = usePrimitives;
        register(RDF.text, String.class);
        register(XSD.anyURI, URI.class);
        register(XSD.booleanType, Boolean.class);
        register(XSD.byteType, Byte.class);               
        register(XSD.decimalType, BigDecimal.class);
        register(XSD.doubleType, Double.class);
        // duration
        register(XSD.floatType, Float.class);
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, Year.class);
        // gYearMonth
        register(XSD.integerType, BigInteger.class);
        register(XSD.intType, Integer.class);
        register(XSD.longType, Long.class);
        register(XSD.shortType, Short.class);
        register(XSD.stringType, String.class);               
        register(RDFS.Literal, String.class);
        
        register(XSD.date, LocalDate.class); // joda-time
        register(XSD.dateTime, DateTime.class); // joda-time
        register(XSD.time, LocalTime.class); // joda-time
        
        defaultType = datatypeToType.get(XSD.stringType);
    }
    
    private void register(UID type, Class<?> clazz) {
        Class<?> primitive = null;
        if (usePrimitives && (primitive = ClassUtils.wrapperToPrimitive(clazz)) != null){
            datatypeToType.put(type, new Type(type, "java.lang", primitive.getSimpleName()));
        }else{
            datatypeToType.put(type, new Type(type, clazz));
        }        
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
