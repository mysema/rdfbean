package com.mysema.rdf.demo.foaf.v1;

import com.mysema.rdf.demo.foaf.domain.Person;

public class Person1 extends Person {

    private MResource resource;

    public void setResource(MResource resource) {
        this.resource = resource;
    }

    public MResource getResource() {
        return resource;
    }
    
    @SuppressWarnings("null")
    public void test() {
        MResource r = null;
        
        for (MProperty prop : r.getProperties()) {
            for (MValue<?> v : prop.getValues()) {
                if (v instanceof MLiteral) {
                    MLiteral<?> ml = (MLiteral<?>) v;
                    System.out.println("Literal: " + ml.getType() + ":" + ml.getValue());
                }
                else if (v instanceof MLocalizedLiteral) {
                    MLocalizedLiteral lv = (MLocalizedLiteral) v;
                    System.out.println("Localized: " + lv.getLocale() + ":" + lv.getValue());
                }
                else if (v instanceof MPlainLiteral) {
                    MPlainLiteral pv = (MPlainLiteral) v;
                    System.out.println("Plain: " + pv.getValue());
                }
                else if (v instanceof MReference) {
                    MReference<?> mv = (MReference<?>) v;
                    System.out.println("Reference:" + mv.getType() + ":" + mv.getId());
                }
                else if (v instanceof MNotMappedReference) {
                    MNotMappedReference mnv = (MNotMappedReference) v;
                    System.out.println("Not mapped reference:" + mnv.getId());
                }
            }
        }
    }
}
