package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;


/**
 * @author tiwe
 *
 */
public class ConfigurationBuilder {
    
    private static final Id DEFAULT_ID = new Id(){
        @Override
        public IDType value() {
            return IDType.LOCAL;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Id.class;
        }
    };
    
    private static final Predicate DEFAULT_PREDICATE = new Predicate(){

        @Override
        public String context() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean ignoreInvalid() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean includeInferred() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean inv() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String ln() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String ns() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            // TODO Auto-generated method stub
            return null;
        }
        
    };
    
    public class MappedClassBuilder {

        private MappedClass mappedClass;
        
        private String idProperty;
        
        public MappedClassBuilder(MappedClass mappedClass) {
            this.mappedClass = mappedClass;            
        }

        public MappedClassBuilder addId(String propertyName) {
            try {
                idProperty = propertyName;
                Field field = mappedClass.getJavaClass().getDeclaredField(propertyName);
                FieldProperty fieldProperty = new FieldProperty(field, new Annotation[]{DEFAULT_ID}, mappedClass);
                MappedPath mappedPath = new MappedPath(fieldProperty, Collections.<MappedPredicate>emptyList(), false);
                mappedClass.addMappedPath(mappedPath);
                return this;
            } catch (SecurityException e) {
                throw new ConfigurationException(e);
            } catch (NoSuchFieldException e) {
                throw new ConfigurationException(e);
            }            
        }

        public MappedClassBuilder addProperties() {
            for (Field field : mappedClass.getJavaClass().getDeclaredFields()){
                if (!field.getName().equals(idProperty)){
                    FieldProperty fieldProperty = new FieldProperty(field, new Annotation[]{DEFAULT_ID}, mappedClass);
                    MappedPath mappedPath = new MappedPath(fieldProperty, Collections.<MappedPredicate>emptyList(), false);
                    mappedClass.addMappedPath(mappedPath);                    
                }
            }
            return this;
        }
        
    }
    
    private final Set<MappedClass> mappedClasses = new HashSet<MappedClass>();
    
    private ConverterRegistry converterRegistry;
    
    private List<FetchStrategy> fetchStrategies = Collections.emptyList();
    
    public Configuration build() {
        if (converterRegistry == null){
            converterRegistry = new ConverterRegistryImpl();
        }
        return new SimpleConfiguration(converterRegistry, fetchStrategies, mappedClasses);
    }

    public MappedClassBuilder addClass(String ns, Class<?> clazz) {
        return addClass(ns, clazz.getSimpleName(), clazz);
    }
    
    public MappedClassBuilder addClass(String ns, String ln, Class<?> clazz) {
        UID uid = new UID(ns, ln);
        MappedClass mappedClass = new MappedClass(clazz, uid, Collections.<MappedClass>emptyList());
        mappedClasses.add(mappedClass);
        return new MappedClassBuilder(mappedClass);
    }

    public void setConverterRegistry(ConverterRegistry converterRegistry) {
        this.converterRegistry = converterRegistry;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }
    
    

}
