package com.mysema.rdfbean.schema;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.util.FileUtils;
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
 * BeanGen provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanGen {
    
    private static final Logger logger = LoggerFactory.getLogger(BeanGen.class);

    private List<UID> skippedSupertypes = Arrays.asList(OWL.Thing, RDFS.Resource);
    
    private Map<String,String> nsToPackage = new HashMap<String,String>();
    
    private Map<UID,TypeModel> datatypeToType = new HashMap<UID,TypeModel>();
    
    private Set<String> exportNamespaces = new HashSet<String>();
    
    private Repository repository;
    
    private TypeModel defaultType;
    
    private boolean oneOfAsEnum = true;
    
    private Serializer serializer = new DefaultSerializer();
    
    public BeanGen(Repository repository){
        this.repository = repository;
        register(XSD.anyURI, URI.class);
        register(XSD.booleanType, Boolean.class);
        register(XSD.byteType, Byte.class);
        register(XSD.date, LocalDate.class);
        register(XSD.dateTime, DateTime.class);        
        register(XSD.dateTime, java.util.Date.class);
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
        register(XSD.stringType, Character.class);
        register(XSD.time, LocalTime.class);        
        register(RDFS.Literal, String.class);
        
        defaultType = datatypeToType.get(XSD.stringType);
    }
    
    private void register(UID type, Class<?> clazz) {
        datatypeToType.put(type, new TypeModel(type, clazz));
    }

    public BeanGen addNamespace(String ns, String packageName){
        nsToPackage.put(ns, packageName);
        return this;
    }    

    public BeanGen addExportNamespace(String ns) {
        exportNamespaces.add(ns);
        return this;
    }        
    
    public BeanGen setDefaultType(TypeModel type){
        defaultType = type;
        return this;
    }
    
    public BeanGen setOneOfAsEnum(boolean oneOfAsEnum) {
        this.oneOfAsEnum = oneOfAsEnum;
        return this;
    }

    public BeanGen setUsePrimitives(boolean usePrimitives) {
        serializer.setUsePrimitives(usePrimitives);
        return this;
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

    private String getPackage(String ns){
        if (nsToPackage.containsKey(ns)){
            return nsToPackage.get(ns);
        }else{
            throw new IllegalArgumentException("No package declared for " + ns);
        }
    }
    
    private void handleClass(RDFSClass<?> rdfType, String targetDir) {
        UID classId = (UID)rdfType.getId();
        if (exportNamespaces.contains(classId.getNamespace())){
            
            // handle enum type
            if (oneOfAsEnum && !rdfType.getOneOf().isEmpty()){
                EnumModel enumType = new EnumModel(classId, 
                        getPackage(classId.getNamespace()),
                        classId.getLocalName());
                
                for (Object object : rdfType.getOneOf()){
                    if (object instanceof MappedResourceBase 
                            && ((MappedResourceBase)object).getId().isURI()){
                        UID id = (UID) ((MappedResourceBase)object).getId();
                        enumType.addEnum(id.getLocalName());
                    }
                }
                
                print(enumType, targetDir);
                
            // handle bean type
            }else{
                BeanModel beanType = new BeanModel(classId, 
                        getPackage(classId.getNamespace()), 
                        classId.getLocalName());
                
                // iterate over supertypes
                for (RDFSClass<?> superType : rdfType.getSuperClasses()){
                    if (!superType.equals(rdfType)                   
                        && !skippedSupertypes.contains(superType.getId())){
                        
                        // handle restriction
                        if (superType instanceof Restriction){
                            Restriction restriction = (Restriction)superType;
                            if (restriction.getOnProperty() != null){
                                beanType.addProperty(handleProperty(restriction.getOnProperty()));
                            }else if (!restriction.getOnProperties().isEmpty()){
                                for (RDFProperty prop : restriction.getOnProperties()){
                                    beanType.addProperty(handleProperty(prop));    
                                }                            
                            }                
                        // handle other supertypes
                        }else if (superType.getId().isURI()){
                            UID superTypeId = (UID)superType.getId();
                            beanType.addSuperType(new TypeModel(
                                    superTypeId,
                                    getPackage(superTypeId.getNamespace()),
                                    superTypeId.getLocalName()
                            ));    
                        }                    
                    }
                }
                
                // iterate over properties
                for (RDFProperty rdfProperty : rdfType.getProperties()){
                    if (rdfProperty.getId().isURI()){
                        beanType.addProperty(handleProperty(rdfProperty));
                    }                    
                }
                print(beanType, targetDir); 
            }
            
        }        
    }
    
    private Writer getWriter(TypeModel type, String targetDir){
        String path = type.getPackageName().replace('.', File.separatorChar) 
            + File.separator 
            + type.getSimpleName() 
            + ".java";
        return FileUtils.writerFor(new File(targetDir, path));
    }

    private void print(BeanModel beanType, String targetDir) {
        Writer w = getWriter(beanType, targetDir);
        try {
            serializer.serialize(beanType, w);
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

    private void print(EnumModel enumType, String targetDir) {        
        Writer w = getWriter(enumType, targetDir);
        try {
            serializer.serialize(enumType, w);
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

    private PropertyModel handleProperty(RDFProperty rdfProperty) {
        UID propertyId = (UID)rdfProperty.getId();
        TypeModel propertyType = defaultType;
        // handle range
        if (!rdfProperty.getRange().isEmpty()){
            RDFSClass<?> range = rdfProperty.getRange().iterator().next();
            if (datatypeToType.containsKey(range.getId())){
                propertyType = datatypeToType.get(range.getId());
            }else if (range.getId().isURI()){
                UID id = (UID)range.getId();
                propertyType = new TypeModel(
                        id,
                        getPackage(id.getNamespace()), 
                        id.getLocalName());
            }
        }
        PropertyModel beanProperty = new PropertyModel(propertyId, 
                propertyId.getLocalName(), 
                propertyType);
        return beanProperty;
    }
    
}
