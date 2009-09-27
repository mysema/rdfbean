/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.query.util.FileUtils;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.rdfs.MappedResourceBase;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;


/**
 * BeanGen provides JavaBean domain generation functionality
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanGen {
    
    // TODO : filter duplicates
    
//    private static final Logger logger = LoggerFactory.getLogger(BeanGen.class);
    
    private static final Pattern normalizePattern = Pattern.compile("[\\-]");
    
    private final Set<String> exportNamespaces = new HashSet<String>();
    
    private final Map<String,String> nsToPackage = new HashMap<String,String>();
    
    private final Map<String,String> nsToClassPrefix = new HashMap<String,String>();
    
    private final Map<String,String> nsToPropertyPrefix = new HashMap<String,String>();
    
    private final Set<UID> localizedProperties = new HashSet<UID>();
    
    private final Set<UID> propertyAsSet = new HashSet<UID>();
    
    private final Set<UID> propertyAsList = new HashSet<UID>();
    
    private boolean oneOfAsEnum = true;
    
    private boolean stripHasOff = true;
    
    private final Repository repository;
    
    private final Serializer serializer;
    
    private List<UID> skippedSupertypes = Arrays.asList(OWL.Thing, RDFS.Resource);
    
    private final TypeMapping typeMapping;
        
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
        this.typeMapping = new TypeMapping(usePrimitives);
    }
    
    public BeanGen addLocalizedProperty(UID... properties){
        for (UID property : properties){
            localizedProperties.add(property);    
        }        
        return this;
    }
    
    public BeanGen addPropertyAsSet(UID... properties){
        for (UID property : properties){
            propertyAsList.add(property);    
        }        
        return this;
    }
    
    public BeanGen addPropertyAsList(UID... properties){
        for (UID property : properties){
            propertyAsSet.add(property);    
        }        
        return this;
    }

    public BeanGen addExportNamespace(String... namespaces) {
        for (String ns : namespaces){
            exportNamespaces.add(ns);    
        }        
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

    private Writer getWriter(Type type, String targetDir){
        String path = type.getPackageName().replace('.', File.separatorChar) 
            + File.separator 
            + type.getSimpleName() 
            + ".java";
        return FileUtils.writerFor(new File(targetDir, path));
    }
    
    private EnumType createEnumType(RDFSClass<?> rdfType, String targetDir, UID classId) {
        EnumType enumType = new EnumType(classId, getPackage(classId), getClassName(classId));        
        for (Object object : rdfType.getOneOf()){
            if (object instanceof MappedResourceBase  && ((MappedResourceBase)object).getId().isURI()){
                UID id = (UID) ((MappedResourceBase)object).getId();
                enumType.addEnum(id.getLocalName());
            }
        }        
//        print(enumType, targetDir);
        return enumType;
    }

    private BeanType createBeanType(RDFSClass<?> rdfType, String targetDir, UID classId) {
        BeanType beanType = new BeanType(classId, getPackage(classId), getClassName(classId));        
        Map<RDFProperty,Property> properties = new HashMap<RDFProperty,Property>();
        
        // iterate over properties
        for (RDFProperty rdfProperty : rdfType.getProperties()){
            if (rdfProperty.getId().isURI()){
                if (!properties.containsKey(rdfProperty)){
                    RDFSClass<?> range = null;
                    if (!rdfProperty.getRange().isEmpty()){
                        range = rdfProperty.getRange().iterator().next();
                    }
                    properties.put(rdfProperty, createProperty(rdfProperty, range));
                }else{
                    // ?!?
                }
            }                    
        }
        
        // iterate over supertypes
        for (RDFSClass<?> superType : rdfType.getSuperClasses()){
            if (!superType.equals(rdfType) && !skippedSupertypes.contains(superType.getId())){
                
                // handle restriction
                if (superType instanceof Restriction){
                    handleRestriction((Restriction)superType, properties);
                                    
                // handle other supertypes
                }else if (superType.getId().isURI()){
                    UID superTypeId = (UID)superType.getId();
                    beanType.addSuperType(new Type(
                            superTypeId,
                            getPackage(superTypeId),
                            getClassName(superTypeId)
                    ));    
                }                    
            }
        }
                
        // add properties to bean model
        for (Property property : properties.values()){
            beanType.addProperty(property);
        }
        
//        print(beanType, targetDir);
        return beanType;
    }

    private void handleRestriction(Restriction restriction, Map<RDFProperty, Property> properties) {
        if (restriction.getHasValue() != null){
            return;
        }        
        Collection<RDFProperty> rdfProperties = Collections.emptySet();        
        if (restriction.getOnProperty() != null){
            rdfProperties = Collections.singleton(restriction.getOnProperty());
        }else if (!restriction.getOnProperties().isEmpty()){
            rdfProperties = restriction.getOnProperties();                                        
        }   
        
        for (RDFProperty rdfProperty : rdfProperties){
            // allValueForm
            Property property;
            if (restriction.getAllValuesFrom() != null && restriction.getAllValuesFrom().getId().isURI()){
                RDFSClass<?> range = restriction.getAllValuesFrom();
                if (properties.containsKey(rdfProperty)){
                    property = properties.get(rdfProperty);
                    property.setType(getPropertyType(rdfProperty, range));
                }else{
                    property = createProperty(rdfProperty, range);
                    properties.put(rdfProperty, property);
                }
            }else{
                if (properties.containsKey(rdfProperty)){
                    property = properties.get(rdfProperty);
                }else{    
                    RDFSClass<?> range = null;
                    if (!rdfProperty.getRange().isEmpty()){
                        range = rdfProperty.getRange().iterator().next();
                    }
                    property = createProperty(rdfProperty, range);
                    properties.put(rdfProperty, property);
                }
            }
            
            for (Integer cardinality : Arrays.asList(
                    restriction.getCardinality(), 
                    restriction.getMinCardinality(), 
                    restriction.getMaxCardinality())){
                if (cardinality != null){
                    property.setMultipleValues(true);
                }
            }
        }
    }
    
    private String getPackage(UID id){
        if (nsToPackage.containsKey(id.getNamespace())){
            return nsToPackage.get(id.getNamespace());
        }else{
            throw new IllegalArgumentException("No package declared for " + id.getNamespace());
        }
    }

    private String getClassName(UID classId) {
        String localName = normalize(classId.getLocalName());
        if (nsToClassPrefix.containsKey(classId.getNamespace())){
            localName = nsToClassPrefix.get(classId.getNamespace()) + localName;
        }
        return localName;
    }

    private static String normalize(String str) {
        return normalizePattern.matcher(str).replaceAll("_");
    }
    
    private String getPropertyName(UID propertyId) {
        String propertyName = normalize(propertyId.getLocalName());
        if (nsToPropertyPrefix.containsKey(propertyId.getNamespace())){
            propertyName = nsToPropertyPrefix.get(propertyId.getNamespace()) + StringUtils.capitalize(propertyName);
        }else if (stripHasOff && propertyName.startsWith("has") && propertyName.length() > 3){
            propertyName = StringUtils.uncapitalize(propertyName.substring(3));
        }
        return propertyName;
    }
    

    private void handleClass(RDFSClass<?> rdfType, String targetDir) throws IOException {
        UID classId = (UID)rdfType.getId();
        if (exportNamespaces.contains(classId.getNamespace())){
            Type type;
            if (oneOfAsEnum && !rdfType.getOneOf().isEmpty()){
                type = createEnumType(rdfType, targetDir, classId);                
            }else{
                type = createBeanType(rdfType, targetDir, classId); 
            }       
            print(type, targetDir);
        }        
    }
    

    
    private Property createProperty(RDFProperty rdfProperty, @Nullable RDFSClass<?> range) {
        UID propertyId = (UID)rdfProperty.getId();
        Type propertyType = getPropertyType(rdfProperty, range);
        String propertyName = getPropertyName(propertyId);        
        return new Property(propertyId, propertyName, propertyType);
    }

    private Type getPropertyType(RDFProperty rdfProperty, @Nullable RDFSClass<?> range) {
        Type propertyType = typeMapping.getDefaultType();
        if (localizedProperties.contains(rdfProperty.getId())){
            propertyType = typeMapping.get(RDF.text);
        }else if (range != null){
            if (typeMapping.containsKey(range.getId())){
                propertyType = typeMapping.get(range.getId());
            }else if (range.getId().isURI()){
                UID id = (UID)range.getId();
                propertyType = new Type(id, getPackage(id),  getClassName(id));
            }
        }        
        if (propertyAsList.contains(rdfProperty.getId())){
            propertyType = new ListType(propertyType);
        }else if (propertyAsSet.contains(rdfProperty.getId())){
            propertyType = new SetType(propertyType);
        }        
        return propertyType;
    }

    public void handleOWLSchema(String targetDir) throws IOException{
        handleSchema(targetDir, RDFSClass.class.getPackage(), OWLClass.class.getPackage());
    }
    
    public void handleRDFSchema(String targetDir) throws IOException{
        handleSchema(targetDir, RDFSClass.class.getPackage());
    }
    
    private void handleSchema(String targetDir, Package...packages) throws IOException{
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl(Locale.ENGLISH);
        sessionFactory.setConfiguration(new DefaultConfiguration(packages));
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        Session session =  sessionFactory.openSession();
        
        // iterate over classes
        for (RDFSClass<?> rdfType : session.findInstances(RDFSClass.class)){
            if (rdfType.getId().isURI()){                
                handleClass(rdfType, targetDir);
            }
        }
        
        session.close();
        sessionFactory.close();
    }

    private void print(Type type, String targetDir) throws IOException {
        Writer w = getWriter(type, targetDir);
        try {
            if (type instanceof BeanType){
                serializer.serialize(((BeanType)type), w);    
            }else if (type instanceof EnumType){
                serializer.serialize(((EnumType)type), w);
            }else{
                throw new IllegalArgumentException("Illegal type " + type); 
            }
            
        }finally{
            w.close();
        }
    }

    public BeanGen setDefaultType(Type type){
        typeMapping.setDefaultType(type);
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
