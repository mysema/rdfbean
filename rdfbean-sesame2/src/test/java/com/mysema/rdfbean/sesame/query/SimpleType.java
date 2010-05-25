/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.MapElements;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * TestType provides
 *
 * @author tiwe
 * @version $Id$
 */
@ClassMapping(ns =TEST.NS, ln="TestType1")
public class SimpleType{        
    @Id 
    private String id;        
    
    @Predicate(ln="directProperty1")  
    private String directProperty;
    
    @Predicate
    @Localized
    private String localizedProperty;
    
    @Predicate(ln="localizedProperty")
    @Localized
    private Map<Locale,String> localizedAsMap;
    
    @Predicate 
    private String notExistantProperty;
    
    @Predicate
    private int numericProperty;
    
    @Predicate(ln="listProperty") 
    private List<SimpleType2> listProperty;
    
    @Predicate(ln="setProperty") 
    private Set<SimpleType2> setProperty;        
    
    @Predicate(ln="setProperty")
    @MapElements(key=@Predicate(ln="directProperty2"))
    private Map<String,SimpleType2> mapProperty;
    
    @Predicate 
    private Date dateProperty;

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