package com.mysema.rdfbean.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mysema.rdfbean.owl.OWL;

/**
 * @author tiwe
 * 
 */
public final class Nodes {

    private static final Map<String, Set<UID>> namespaces = new HashMap<String, Set<UID>>();

    public static final Set<UID> all;

    static {
        Set<UID> uids = new HashSet<UID>();
        try {
            for (Class<?> cl : Arrays.<Class<?>> asList(DC.class, DCTERMS.class, FOAF.class, GEO.class,
                    OWL.class, RDF.class, RDFS.class, SKOS.class, XSD.class)) {
                handleClass(cl, uids);
            }
            all = Collections.unmodifiableSet(uids);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

    }

    private static void handleClass(Class<?> cl, Set<UID> uids) throws IllegalAccessException {
        Set<UID> ns = new HashSet<UID>();
        for (Field field : cl.getDeclaredFields()) {
            if (field.getType().equals(UID.class)) {
                UID uid = (UID) field.get(null);
                ns.add(uid);
            }
        }
        uids.addAll(ns);
        namespaces.put(ns.iterator().next().ns(), Collections.unmodifiableSet(ns));
    }

    public static Set<UID> get(String ns) {
        return namespaces.get(ns);
    }

    private Nodes() {
    }

}
