package com.mysema.rdf.demo.foaf.v2;

import java.util.Locale;
import java.util.Map;

import com.mysema.rdfbean.model.UID;

public interface Resource {

    Map<UID, Literal> getValues();
    Map<UID, Reference> getReferences();
    Map<UID, Map<Locale, Literal>> getLocalizedValues();

    String getLabel(UID uid);
}