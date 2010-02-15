/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDFS;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = RDFS.NS, ln = "Resource")
public class RDFSResource extends MappedResourceBase {

    @Predicate(ln = "comment")
    @Localized
    private Map<Locale, String> comments = new LinkedHashMap<Locale, String>();

    @Predicate(ln = "label")
    @Localized
    private Map<Locale, String> labels = new LinkedHashMap<Locale, String>();

    public RDFSResource() {
        super();
    }

    public RDFSResource(ID id) {
        super(id);
    }

    public Map<Locale, String> getComments() {
        return Collections.unmodifiableMap(comments);
    }

    public Map<Locale, String> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public void setComment(Locale locale, String comment) {
        comments.put(locale, comment);
    }

    public void setLabel(Locale locale, String label) {
        labels.put(locale, label);
    }

}
