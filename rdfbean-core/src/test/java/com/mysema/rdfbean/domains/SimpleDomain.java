/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.MapPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SetPath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.MapElements;
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;

public interface SimpleDomain {
    
    @ClassMapping(ns =TEST.NS, ln="TestType1")
    public class SimpleType{        
        @Id 
        public String id;        
        
        @Predicate(ln="directProperty1")  
        public String directProperty;
        
        @Predicate
        @Localized
        public String localizedProperty;
        
        @Predicate(ln="localizedProperty")
        @Localized
        public Map<Locale,String> localizedAsMap;
        
        @Predicate 
        public String notExistantProperty;
        
        @Predicate
        public int numericProperty;
        
        @Predicate(ln="listProperty")
        public List<SimpleType2> listProperty;
        
        @Predicate(ln="setProperty") 
        public Set<SimpleType2> setProperty;        
        
        @Predicate(ln="setProperty")
        @MapElements(key=@Predicate(ln="directProperty2"))
        public Map<String,SimpleType2> mapProperty;
        
        @Predicate 
        public Date dateProperty;

        public SimpleType() {}
        
        public SimpleType(String property) {
            this.directProperty = property;
        }
        
        public String getId() {
            return id;
        }

        public String getDirectProperty() {
            return directProperty;
        }

        public String getLocalizedProperty() {
            return localizedProperty;
        }

        public Map<Locale, String> getLocalizedAsMap() {
            return localizedAsMap;
        }

        public String getNotExistantProperty() {
            return notExistantProperty;
        }

        public int getNumericProperty() {
            return numericProperty;
        }

        public List<SimpleType2> getListProperty() {
            return listProperty;
        }

        public Set<SimpleType2> getSetProperty() {
            return setProperty;
        }

        public Date getDateProperty() {
            return dateProperty;
        }

        public Map<String, SimpleType2> getMapProperty() {
            return mapProperty;
        }

        public void setDirectProperty(String directProperty) {
            this.directProperty = directProperty;
        }

        public void setLocalizedProperty(String localizedProperty) {
            this.localizedProperty = localizedProperty;
        }

        public void setLocalizedAsMap(Map<Locale, String> localizedAsMap) {
            this.localizedAsMap = localizedAsMap;
        }

        public void setNotExistantProperty(String notExistantProperty) {
            this.notExistantProperty = notExistantProperty;
        }

        public void setNumericProperty(int numericProperty) {
            this.numericProperty = numericProperty;
        }

        public void setListProperty(List<SimpleType2> listProperty) {
            this.listProperty = listProperty;
        }

        public void setSetProperty(Set<SimpleType2> setProperty) {
            this.setProperty = setProperty;
        }

        public void setMapProperty(Map<String, SimpleType2> mapProperty) {
            this.mapProperty = mapProperty;
        }

        public void setDateProperty(Date dateProperty) {
            this.dateProperty = dateProperty;
        }
        
        
    }
    
    @ClassMapping(ns = TEST.NS, ln="TestType2")
    public class SimpleType2{

        @Id 
        public String id;        
            
        @Path({@Predicate(ln="testType"), @Predicate(ln="directProperty")}) 
        public String pathProperty;                       
        
        @Predicate(ln="directProperty2") 
        public String directProperty;
        
        public String getId() {
            return id;
        }

        public String getPathProperty() {
            return pathProperty;
        }

        public String getDirectProperty() {
            return directProperty;
        }            

        public void setPathProperty(String pathProperty) {
            this.pathProperty = pathProperty;
        }

        public void setDirectProperty(String directProperty) {
            this.directProperty = directProperty;
        }

        
    }
    
    public class QSimpleType extends EntityPathBase<SimpleDomain.SimpleType> {

        private static final long serialVersionUID = -243400857;

        public static final QSimpleType simpleType = new QSimpleType("simpleType");

        public final DateTimePath<java.util.Date> dateProperty = createDateTime("dateProperty", java.util.Date.class);

        public final StringPath directProperty = createString("directProperty");

        public final StringPath id = createString("id");

        public final ListPath<SimpleDomain.SimpleType2, QSimpleType2> listProperty = this.<SimpleType2, QSimpleType2>createList("listProperty", SimpleDomain.SimpleType2.class, QSimpleType2.class);

        public final MapPath<java.util.Locale, String, StringPath> localizedAsMap = this.<java.util.Locale, String, StringPath>createMap("localizedAsMap", java.util.Locale.class, String.class, StringPath.class);

        public final StringPath localizedProperty = createString("localizedProperty");

        public final MapPath<String, SimpleDomain.SimpleType2, QSimpleType2> mapProperty = this.<String, SimpleDomain.SimpleType2, QSimpleType2>createMap("mapProperty", String.class, SimpleDomain.SimpleType2.class, QSimpleType2.class);

        public final StringPath notExistantProperty = createString("notExistantProperty");

        public final NumberPath<Integer> numericProperty = createNumber("numericProperty", Integer.class);

        public final SetPath<SimpleDomain.SimpleType2, QSimpleType2> setProperty = this.<SimpleType2, QSimpleType2>createSet("setProperty", SimpleDomain.SimpleType2.class, QSimpleType2.class);

        public QSimpleType(String variable) {
            super(SimpleDomain.SimpleType.class, forVariable(variable));
        }

        public QSimpleType(EntityPathBase<? extends SimpleDomain.SimpleType> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QSimpleType(PathMetadata<?> metadata) {
            super(SimpleDomain.SimpleType.class, metadata);
        }

    }
    
    public class QSimpleType2 extends EntityPathBase<SimpleDomain.SimpleType2> {

        private static final long serialVersionUID = 1044508075;

        public static final QSimpleType2 simpleType2 = new QSimpleType2("simpleType2");

        public final StringPath directProperty = createString("directProperty");

        public final StringPath id = createString("id");

        public final StringPath pathProperty = createString("pathProperty");

        public QSimpleType2(String variable) {
            super(SimpleDomain.SimpleType2.class, forVariable(variable));
        }

        public QSimpleType2(EntityPathBase<? extends SimpleDomain.SimpleType2> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QSimpleType2(PathMetadata<?> metadata) {
            super(SimpleDomain.SimpleType2.class, metadata);
        }

    }

}
