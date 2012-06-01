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
    
    private final Class<?> parent;
    
    public ClassMappingImpl(UID id, Class<?> parent) {
        this(id.getNamespace(), id.getLocalName(), parent);
    }
    
    public ClassMappingImpl(String ns, String ln, Class<?> parent) {
        this.ns = ns;
        this.ln = ln;
        this.parent = parent;
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

    @Override
    public Class<?> parent() {
	return parent;
    }

}
