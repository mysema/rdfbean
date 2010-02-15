/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSResource;

/**
 * @author sasa
 * 
 */
@ClassMapping
public class TypedList extends OWLClass {

    private static final RDFSClass<RDFSResource> List = new RDFSClass<RDFSResource>(RDF.List);

    private static final RDFProperty first = new RDFProperty(RDF.first);

    private static final RDFProperty rest = new RDFProperty(RDF.rest);

    public TypedList(RDFSClass<?> componentType) {
        this(null, componentType);
    }

    public TypedList(@Nullable String ns, RDFSClass<?> componentType) {
        super(getUID(ns, componentType));
        ID id = getId();
        if (id != null) {
            setLabel(Locale.ROOT, ((UID) id).ln());
        }
        addSuperClass(List);
        allValuesFrom(first, componentType);
        allValuesFrom(rest, this);
    }

    @Nullable
    private static UID getUID(@Nullable String ns, RDFSClass<?> componentType) {
        ID id = componentType.getId();
        if (id instanceof UID) {
            UID uid = (UID) id;
            String prefix = uid.ns().substring(uid.ns().lastIndexOf('/') + 1,
                    uid.ns().length() - 1);
            if (StringUtils.isEmpty(ns)) {
                ns = uid.ns();
            }
            return new UID(ns, prefix + "-" + uid.ln() + "-TypedList");
        } else {
            return null;
        }
    }

    private void allValuesFrom(RDFProperty property, RDFSClass<?> clazz) {
        Restriction listRestriction = new Restriction();
        listRestriction.setOnProperty(property);
        listRestriction.setAllValuesFrom(clazz);
        addSuperClass(listRestriction);
    }

}
