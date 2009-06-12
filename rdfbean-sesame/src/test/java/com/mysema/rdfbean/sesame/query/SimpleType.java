/**
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
    String id;        
    
    @Predicate(ln="directProperty1")  
    String directProperty;
    
    @Predicate
    @Localized
    String localizedProperty;
    
    @Predicate(ln="localizedProperty")
    @Localized
    Map<Locale,String> localizedAsMap;
    
    @Predicate 
    String notExistantProperty;
    
    @Predicate
    int numericProperty;
    
    @Predicate(ln="listProperty") 
    List<SimpleType2> listProperty;
    
    @Predicate(ln="setProperty") 
    Set<SimpleType2> setProperty;        
    
    @Predicate 
    Date dateProperty;

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
    
    
    
}