package com.mysema.rdf.demo.foaf.v2;

import java.util.Collection;
import java.util.Locale;

import com.mysema.rdfbean.model.UID;

/*
 * person.firstName
 * 
 * <#list person.resources as resource>
 *   ${resource.label}
 *  <#list resource.values as value> 
 *    ${value.value}
 *  </#list>
 *  <#list resource.nodes as node> 
 *    ${value.value}
 *  </#list>
 *  
 *  <#list resource.references as reference> 
 *    ${value.value}
 *  </#list>
 * </#list>
 */

public interface Resource extends Node {
    
    public UID getUid();
    
    public String getLabel();
    public void   setLabel(String label);
    
    public Collection<Value<?>> getValues();
    
    public Collection<Literal> getValues(Locale locale);
    public Collection<Locale> getLocales();
    
    public int               getValueCount();
    public void              setValues(Collection<Value<?>> values);
    
    
}