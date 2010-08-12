/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;
import com.mysema.query.codegen.BeanSerializer;
import com.mysema.query.codegen.EntityType;
import com.mysema.query.codegen.Property;
import com.mysema.query.codegen.SimpleSerializerConfig;
import com.mysema.query.codegen.Supertype;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.ClassMappingImpl;
import com.mysema.rdfbean.object.PredicateImpl;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;


/**
 * JavaBeanExporter provides JavaBean domain generation functionality
 *
 * @author tiwe
 * @version $Id$
 */
public class JavaBeanExporter {
    
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
    
    private List<UID> skippedSupertypes = Arrays.asList(OWL.Thing, RDFS.Resource);
    
    private final TypeMapping typeMapping;
    
    public JavaBeanExporter(boolean usePrimitives){
        this.typeMapping = new TypeMapping(usePrimitives);
    }
    
    public JavaBeanExporter addClassPrefix(String ns, String prefix){
        nsToClassPrefix.put(ns, prefix);
        return this;
    }
    
    public JavaBeanExporter addExportNamespaces(String... namespaces) {
        for (String ns : namespaces){
            exportNamespaces.add(ns);    
        }        
        return this;
    }
    
    public JavaBeanExporter addLocalizedProperty(UID... properties){
        for (UID property : properties){
            localizedProperties.add(property);    
        }        
        return this;
    }

    public JavaBeanExporter addPackage(String ns, String packageName){
        nsToPackage.put(ns, packageName);
        return this;
    }    

    public JavaBeanExporter addPropertyAsList(UID... properties){
        for (UID property : properties){
            propertyAsSet.add(property);    
        }        
        return this;
    }
    
    public JavaBeanExporter addPropertyAsSet(UID... properties){
        for (UID property : properties){
            propertyAsList.add(property);    
        }        
        return this;
    }
    
    public JavaBeanExporter addPropertyPrefix(String ns, String prefix){
        nsToPropertyPrefix.put(ns, prefix);
        return this;
    }        
    
    private EntityType createBeanType(RDFSClass<?> rdfType) {
        // type
        UID id = rdfType.getId().asURI();
        String pkgName = getPackage(id);
        String simpleName = getClassName(id);
        Type type = new SimpleType(TypeCategory.ENTITY, pkgName+"."+simpleName, pkgName, id.getLocalName(), false, false);
        EntityType entityType = new EntityType("Q", type);
        entityType.addAnnotation(new ClassMappingImpl(id.getNamespace(),""));
        
        Map<RDFProperty, Property> properties = new HashMap<RDFProperty, Property>();
        
        // iterate over properties
        for (RDFProperty rdfProperty : rdfType.getProperties()){
            if (rdfProperty.getId().isURI()){
                if (!properties.containsKey(rdfProperty)){
                    RDFSClass<?> range = null;
                    if (!rdfProperty.getRange().isEmpty()){
                        range = rdfProperty.getRange().iterator().next();
                    }
                    properties.put(rdfProperty, createProperty(entityType, id, rdfProperty, range));
                }
            }                    
        }
        
        // iterate over supertypes
        for (RDFSClass<?> superType : rdfType.getSuperClasses()){
            if (superType != null && !superType.equals(rdfType) && !skippedSupertypes.contains(superType.getId())){
                
                // handle restriction
                if (superType instanceof Restriction){
                    handleRestriction(entityType, id, (Restriction)superType, properties);
                                    
                // handle other supertypes
                }else if (superType.getId().isURI()){
                    UID superTypeId = (UID)superType.getId();
                    entityType.addSupertype(new Supertype(getJavaType(superTypeId)));
                }                    
            }
        }
                
        // add properties to bean model
        for (Property property : properties.values()){
            entityType.addProperty(property);
        }
        
        return entityType;
    }

    private EntityType createEnumType(RDFSClass<?> rdfType) {
//        EnumType enumType = new EnumType(classId, getPackage(classId), getClassName(classId));        
//        for (Object object : rdfType.getOneOf()){
//            if (object instanceof MappedResourceBase  && ((MappedResourceBase)object).getId().isURI()){
//                UID id = (UID) ((MappedResourceBase)object).getId();
//                enumType.addEnum(id.getLocalName());
//            }
//        }        
//        return enumType;
        
     // type
        UID id = rdfType.getId().asURI();
        String pkgName = getPackage(id);
        String simpleName = getClassName(id);
        Type type = new SimpleType(TypeCategory.ENTITY, pkgName+"."+simpleName, pkgName, id.getLocalName(), false, false);
        EntityType entityType = new EntityType("Q", type);
        entityType.addAnnotation(new ClassMappingImpl(id.getNamespace(),""));
        return entityType;
    }

    private Property createProperty(EntityType entityType, UID entityId, RDFProperty rdfProperty, @Nullable RDFSClass<?> range) {
        UID propertyId = rdfProperty.getId().asURI();
        Type propertyType = getPropertyType(rdfProperty, range);
        String propertyName = getPropertyName(propertyId);        
        Property property = new Property(entityType, propertyName, propertyType, new String[0]);
        if (propertyId.getNamespace().equals(entityId.getNamespace())){
            property.addAnnotation(new PredicateImpl("","",propertyId.getLocalName(),false));
        }else{
            property.addAnnotation(new PredicateImpl("",propertyId,false));
        }
        return property;
    }
    
    public void export(Session session, File targetFolder) throws IOException{        
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        
        // collect entity types
        for (OWLClass owlClass : session.findInstances(OWLClass.class)){
            if (owlClass.getId().isURI()){
                EntityType entityType;
                if (oneOfAsEnum && !owlClass.getOneOf().isEmpty()){
                    entityType = createEnumType(owlClass);                
                }else{
                    entityType = createBeanType(owlClass); 
                }
                entityTypes.add(entityType);
            }            
        }
        
        // serialize
        BeanSerializer beanSerializer = new BeanSerializer();
        for (EntityType entityType : entityTypes){
            File folder = new File(targetFolder, entityType.getPackageName().replace('.', '/'));
            folder.mkdirs();
            File javaFile = new File(folder, entityType.getSimpleName()+".java");
            javaFile.createNewFile();
            Writer writer = new OutputStreamWriter(new FileOutputStream(javaFile), "UTF-8");
            try{
                beanSerializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));    
            }finally{
                writer.close();
            }            
            
        }
    }
    
    private Type getJavaType(UID id){
        String typePackage = getPackage(id);
        String typeName = getClassName(id);
        return new SimpleType(TypeCategory.ENTITY, typePackage + "." + typeName, typePackage, typeName, false, false);
    }

    private String getClassName(UID classId) {
        String localName = normalize(classId.getLocalName());
        if (nsToClassPrefix.containsKey(classId.getNamespace())){
            localName = nsToClassPrefix.get(classId.getNamespace()) + localName;
        }
        return localName;
    }

    private String getPackage(UID id){
        if (nsToPackage.containsKey(id.getNamespace())){
            return nsToPackage.get(id.getNamespace());
        }else{
            throw new IllegalArgumentException("No package declared for " + id.getNamespace());
        }
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

    private Type getPropertyType(RDFProperty rdfProperty, @Nullable RDFSClass<?> range) {
        Type propertyType = typeMapping.getDefaultType();
        if (localizedProperties.contains(rdfProperty.getId())){
            propertyType = typeMapping.get(RDF.text);
        }else if (range != null){
            if (typeMapping.containsKey(range.getId())){
                propertyType = typeMapping.get(range.getId());
            }else if (range.getId().isURI()){
                UID id = (UID)range.getId();
                String pkgName = getPackage(id);
                String simpleName = getClassName(id);
                propertyType = new SimpleType(TypeCategory.SIMPLE, pkgName+"."+simpleName, pkgName, simpleName, false, false);
            }
        }        
        if (propertyAsList.contains(rdfProperty.getId())){
            propertyType = new SimpleType(Types.LIST, propertyType);
        }else if (propertyAsSet.contains(rdfProperty.getId())){
            propertyType = new SimpleType(Types.LIST, propertyType);
        }        
        return propertyType;
    }

    private void handleRestriction(EntityType entityType, UID entityId, Restriction restriction, Map<RDFProperty, Property> properties) {
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
//                    property.setType(getPropertyType(rdfProperty, range));
                }else{
                    property = createProperty(entityType, entityId, rdfProperty, range);
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
                    property = createProperty(entityType, entityId, rdfProperty, range);
                    properties.put(rdfProperty, property);
                }
            }

            // FIXME
//            for (Integer cardinality : Arrays.asList(
//                    restriction.getCardinality(), 
//                    restriction.getMinCardinality(), 
//                    restriction.getMaxCardinality())){
//                if (cardinality != null){
//                    property.setMultipleValues(true);
//                }
//            }
        }
    }
    
    private String normalize(String str) {
        return normalizePattern.matcher(str).replaceAll("_");
    }
    
    public JavaBeanExporter setDefaultType(Type type){
        typeMapping.setDefaultType(type);
        return this;
    }

    public JavaBeanExporter setOneOfAsEnum(boolean b) {
        this.oneOfAsEnum = b;
        return this;
    }
    
    public JavaBeanExporter setStripHasOff(boolean b) {
        this.stripHasOff = b;
        return this;
    }
    
}
