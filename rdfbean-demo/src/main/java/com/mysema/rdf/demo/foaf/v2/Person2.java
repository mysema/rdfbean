package com.mysema.rdf.demo.foaf.v2;

import java.util.Locale;

import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.foaf.v2.Value.Type;
import com.mysema.rdfbean.model.UID;

public class Person2 extends Person {

    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
    
    public void handleLiteral(Literal lit) {
        System.out.println(
                lit.getLocale() == null ? lit.getType() : lit.getLocale()
                        + ":" + lit.getValue());
    }

    public void handleUID(Value<UID> value) {
        System.out.println("UID:" + value.getValue());
    }
    
    public void handleCollection(TypedCollection collection) {
        for (Value<?> subValue : collection.getValue()) {
            handleValue(subValue);
        }   
    }
    
    @SuppressWarnings("unchecked")
    public void handleValue(Value<?> value) {
        if (value.getType() == Type.LITERAL) {
            handleLiteral((Literal) value); 
        }
        else if (value.getType() == Type.UID) {
            handleUID((Value<UID>) value);
        }
        else if (value.getType() == Type.RESOURCE) {
            handleResource((Resource)value.getValue());
        }
        else if (value.getType() == Type.COLLECTION) {
            handleCollection((TypedCollection) value);
        }
    }
    
    public void handleResource(Resource r) {
        
        for (Value<?> value : r.getValues()) {
            handleValue(value);
        }
        
//        for (UID uid : r.getValues().keySet()) {
//            System.out.println(uid + ":" + r.getValues().get(uid).getValue());
//        }
//        
//        for (UID uid : r.getLocalizedValues().keySet()) {
//            for (Locale locale: r.getLocalizedValues().get(uid).keySet()) {
//                System.out.println(uid + ": (" + locale + ") "+ r.getLocalizedValues().get(uid).getValue());
//            }
//        }
        
//        for (MProperty prop : r.getProperties()) {
//            for (Value<?> v : prop.getValues()) {
//                if (v instanceof Literal) {
//                    Literal<?> ml = (Literal<?>) v;
//                    System.out.println("Literal: " + ml.getType() + ":" + ml.getValue());
//                }
//                else if (v instanceof MLocalizedLiteral) {
//                    MLocalizedLiteral lv = (MLocalizedLiteral) v;
//                    System.out.println("Localized: " + lv.getLocale() + ":" + lv.getValue());
//                }
//                else if (v instanceof MPlainLiteral) {
//                    MPlainLiteral pv = (MPlainLiteral) v;
//                    System.out.println("Plain: " + pv.getValue());
//                }
//                else if (v instanceof Reference) {
//                    Reference<?> mv = (Reference<?>) v;
//                    System.out.println("Reference:" + mv.getType() + ":" + mv.getId());
//                }
//                else if (v instanceof MNotMappedReference) {
//                    MNotMappedReference mnv = (MNotMappedReference) v;
//                    System.out.println("Not mapped reference:" + mnv.getId());
//                }
//            }
//        }
    }
}
/*
 * @ClassMapping(ns=TEST.NS)
    public static class Reftype {
        @Id
        ID id;
        
        @Predicate(ns=RDF.NS)
        String label;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Genbean {
        // Single valued - value type?!?
        Map<UID, Object> singleValued;
        
        // Multi valued - value type?!?
        Map<UID, Collection<Object>> multiValued;
        
        // Localized string
        @Localized
        Map<UID, String> localized; // Not supported in generic access as such?

        // Localized strings
        Map<UID, Map<Locale, String>> localizedMap;
        
        // References
        Map<UID, Reftype> typedReference;
        Map<UID, ID> idReference;
*/