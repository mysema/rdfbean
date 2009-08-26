package com.mysema.rdf.demo.foaf.v2;

import java.util.Locale;

public interface Literal extends Value<String> {
    
    Locale getLocale();
    void   setLocale(Locale locale);
    
    String getLiteralType();
    void   setLiteralType(String type);
}
