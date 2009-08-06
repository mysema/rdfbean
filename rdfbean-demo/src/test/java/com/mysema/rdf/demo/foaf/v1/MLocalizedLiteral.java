package com.mysema.rdf.demo.foaf.v1;

import java.util.Locale;

public interface MLocalizedLiteral extends MValue<String> {

    Locale getLocale();
    void setLocale(Locale locale);
    
}
