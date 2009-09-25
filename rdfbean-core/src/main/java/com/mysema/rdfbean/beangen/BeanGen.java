/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.util.FileUtils;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.rdfs.MappedResourceBase;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.xsd.Year;


/**
 * BeanGen provides JavaBean domain generation functionality
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanGen {
    
    // TODO : normalize class and property localNames
    
    // TODO : single-value / multi-value
    
    // TODO : filter duplicates
    
    // TODO : rdf:text for Localized
    
    private static final Logger logger = LoggerFactory.getLogger(BeanGen.class);

    private Map<UID,TypeModel> datatypeToType = new HashMap<UID,TypeModel>();
    
    private TypeModel defaultType;
    
    private Set<String> exportNamespaces = new HashSet<String>();
    
    private Map<String,String> nsToPackage = new HashMap<String,String>();
    
    private Map<String,String> nsToClassPrefix = new HashMap<String,String>();
    
    private Map<String,String> nsToPropertyPrefix = new HashMap<String,String>();
    
    private Set<UID> localizedProperties = new HashSet<UID>();
    
    private boolean oneOfAsEnum = true;
    
    private boolean stripHasOff = true;
    
    private final boolean usePrimitives;
    
    private Repository repository;
    
    private final Serializer serializer;
    
    private List<UID> skippedSupertypes = Arrays.asList(OWL.Thing, RDFS.Resource);
    
    public BeanGen(Repository repository){
        this(repository, new DefaultSerializer(), true);
    }
    
    public BeanGen(Repository repository, boolean usePrimitives){
        this(repository, new DefaultSerializer());
    }
        
    public BeanGen(Repository repository, Serializer serializer){
        this(repository, serializer, true);
    }
        
    public BeanGen(Repository repository, Serializer serializer, boolean usePrimitives){
        this.repository = repository;
        this.serializer = serializer;
        this.usePrimitives = usePrimitives;
        
        register(XSD.anyURI, URI.class);
        register(XSD.booleanType, Boolean.class);
        register(XSD.byteType, Byte.class);
        register(XSD.date, LocalDate.class); // joda-time
        register(XSD.dateTime, DateTime.class); // joda-time       
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
        register(XSD.time, LocalTime.class); // joda-time       
        register(RDFS.Literal, String.class);
        
        defaultType = datatypeToType.get(XSD.stringType);
    }
    
    private void add(PropertyModel property, Map<ID, PropertyModel> properties) {
        ID id = property.getRdfProperty();
        if (properties.containsKey(id)){
            property = property.merge(properties.get(id), defaultType);
        }
        properties.put(id, property);
    }
    
    public BeanGen addLocalizedProperty(UID property){
        localizedProperties.add(property);
        return this;
    }

    public BeanGen addExportNamespace(String ns) {
        exportNamespaces.add(ns);
        return this;
    }    

    public BeanGen addPackage(String ns, String packageName){
        nsToPackage.put(ns, packageName);
        return this;
    }
    
    public BeanGen addPropertyPrefix(String ns, String prefix){
        nsToPropertyPrefix.put(ns, prefix);
        return this;
    }
    
    public BeanGen addClassPrefix(String ns, String prefix){
        nsToClassPrefix.put(ns, prefix);
        return this;
    }        

    private Writer getWriter(TypeModel type, String targetDir){
        String path = type.getPackageName().replace('.', File.separatorChar) 
            + File.separator 
            + type.getSimpleName() 
            + ".java";
        return FileUtils.writerFor(new File(targetDir, path));
    }
    
    private void handleEnum(RDFSClass<?> rdfType, String targetDir, UID classId) {
        EnumModel enumType = new EnumModel(classId, getPackage(classId), getLocalName(classId));        
        for (Object object : rdfType.getOneOf()){
            if (object instanceof MappedResourceBase  && ((MappedResourceBase)object).getId().isURI()){
                UID id = (UID) ((MappedResourceBase)object).getId();
                enumType.addEnum(id.getLocalName());
            }
        }        
        print(enumType, targetDir);
    }

    // TODO : simplify
    private void handleBean(RDFSClass<?> rdfType, String targetDir, UID classId) {
        BeanModel beanType = new BeanModel(classId, getPackage(classId), getLocalName(classId));        
        Map<ID,PropertyModel> properties = new HashMap<ID,PropertyModel>();
        
        // iterate over supertypes
        for (RDFSClass<?> superType : rdfType.getSuperClasses()){
            if (!superType.equals(rdfType) && !skippedSupertypes.contains(superType.getId())){
                
                // handle restriction
                if (superType instanceof Restriction){
                    Restriction restriction = (Restriction)superType;
                    if (restriction.getHasValue() != null){
                        // can be skipped, since the value is static
                        continue;
                        
                    }else if (restriction.getOnProperty() != null){
                        if (restriction.getAllValuesFrom() != null && restriction.getAllValuesFrom().getId().isURI()){                            
                            add(handleProperty(
                                    restriction.getOnProperty(), 
                                    restriction.getAllValuesFrom()), properties);
                        }else{
                            add(handleProperty(restriction.getOnProperty()), properties);    
                        }                        
                        
                    }else if (!restriction.getOnProperties().isEmpty()){
                        for (RDFProperty prop : restriction.getOnProperties()){
                            add(handleProperty(prop), properties);
                        }                            
                    }                
                // handle other supertypes
                }else if (superType.getId().isURI()){
                    UID superTypeId = (UID)superType.getId();
                    beanType.addSuperType(new TypeModel(
                            superTypeId,
                            getPackage(superTypeId),
                            getLocalName(superTypeId)
                    ));    
                }                    
            }
        }
        
        // iterate over properties
        for (RDFProperty rdfProperty : rdfType.getProperties()){
            if (rdfProperty.getId().isURI()){
                add(handleProperty(rdfProperty), properties);
            }                    
        }
        
        // add properties to bean model
        for (PropertyModel property : properties.values()){
            beanType.addProperty(property);
        }
        
        print(beanType, targetDir);
    }
    
    private String getPackage(UID id){
        if (nsToPackage.containsKey(id.getNamespace())){
            return nsToPackage.get(id.getNamespace());
        }else{
            throw new IllegalArgumentException("No package declared for " + id.getNamespace());
        }
    }

    private String getLocalName(UID classId) {
        String localName = classId.getLocalName();
        if (nsToClassPrefix.containsKey(classId.getNamespace())){
            localName = nsToClassPrefix.get(classId.getNamespace()) + localName;
        }
        return localName;
    }

    private void handleClass(RDFSClass<?> rdfType, String targetDir) {
        UID classId = (UID)rdfType.getId();
        if (exportNamespaces.contains(classId.getNamespace())){            
            if (oneOfAsEnum && !rdfType.getOneOf().isEmpty()){
                handleEnum(rdfType, targetDir, classId);                
            }else{
                handleBean(rdfType, targetDir, classId); 
            }            
        }        
    }
    
    public void handleOWL(String targetDir){
        Session session = SessionUtil.openSession(repository, 
                RDFSClass.class.getPackage(), 
                OWLClass.class.getPackage());
        // iterate over classes
        for (RDFSClass<?> rdfType : session.findInstances(RDFSClass.class)){
            if (rdfType.getId().isURI()){                
                handleClass(rdfType, targetDir);
            }
        }
    }
    
    private PropertyModel handleProperty(RDFProperty rdfProperty) {
        if (!rdfProperty.getRange().isEmpty()){
            return handleProperty(rdfProperty, rdfProperty.getRange().iterator().next());
        }else{
            return handleProperty(rdfProperty, null);    
        }        
    }

    private PropertyModel handleProperty(RDFProperty rdfProperty, RDFSClass<?> range) {
        UID propertyId = (UID)rdfProperty.getId();
        TypeModel propertyType = defaultType;
        // handle range
        if (range != null){
            if (datatypeToType.containsKey(range.getId())){
                propertyType = datatypeToType.get(range.getId());
            }else if (range.getId().isURI()){
                UID id = (UID)range.getId();
                propertyType = new TypeModel(id, getPackage(id),  getLocalName(id));
            }
        }
        String propertyName = propertyId.getLocalName();
        if (nsToPropertyPrefix.containsKey(propertyId.getNamespace())){
            propertyName = nsToPropertyPrefix.get(propertyId.getNamespace()) + StringUtils.capitalize(propertyName);
        }else if (stripHasOff && propertyName.startsWith("has") && propertyName.length() > 3){
            propertyName = StringUtils.uncapitalize(propertyName.substring(3));
        }        
        PropertyModel beanProperty = new PropertyModel(propertyId, propertyName, propertyType);
        return beanProperty;
    }
    
    public void handleRDFSchema(String targetDir){
        Session session = SessionUtil.openSession(repository, RDFSClass.class.getPackage());        
        // iterate over classes
        for (RDFSClass<?> rdfType : session.findInstances(RDFSClass.class)){
            if (rdfType.getId().isURI()){                
                handleClass(rdfType, targetDir);
            }
        }
    }

    private void print(TypeModel type, String targetDir) {
        Writer w = getWriter(type, targetDir);
        try {
            if (type instanceof BeanModel){
                serializer.serialize(((BeanModel)type), w);    
            }else if (type instanceof EnumModel){
                serializer.serialize(((EnumModel)type), w);
            }else{
                throw new IllegalArgumentException("Illegal type " + type); 
            }
            
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            try {
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void register(UID type, Class<?> clazz) {
        Class<?> primitive = null;
        if (usePrimitives && (primitive = ClassUtils.wrapperToPrimitive(clazz)) != null){
            datatypeToType.put(type, new TypeModel(type, "java.lang", primitive.getSimpleName()));
        }else{
            datatypeToType.put(type, new TypeModel(type, clazz));
        }        
    }

    public BeanGen setDefaultType(TypeModel type){
        defaultType = type;
        return this;
    }

    public BeanGen setOneOfAsEnum(boolean b) {
        this.oneOfAsEnum = b;
        return this;
    }
    
    public BeanGen setStripHasOff(boolean b) {
        this.stripHasOff = b;
        return this;
    }
    
}
