package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
@SuppressWarnings("all")
public class ClassMappingImpl implements ClassMapping{
    
    private final String ln, ns;
    
    public ClassMappingImpl(UID id) {
        ns = id.getNamespace();
        ln = id.getLocalName();
    }
    
    public ClassMappingImpl(String ns, String ln) {
        this.ns = ns;
        this.ln = ln;
    }
    

    @Override
    public String ln() {
        return ln;
    }

    @Override
    public String ns() {
        return ns;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ClassMapping.class;
    }

}
