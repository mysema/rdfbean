package com.mysema.rdfbean.schema;

import com.mysema.rdfbean.model.UID;

/**
 * Property provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PropertyModel implements Comparable<PropertyModel>{

    private final UID rdfProperty;
    
    private final String name;
    
    private final TypeModel type;
    
    private TypeModel keyType;
    
    private TypeModel valueType;
    
    public PropertyModel(UID rdfProperty, String name, TypeModel type){
        this.rdfProperty = rdfProperty;
        this.name = name;
        this.type = type;
    }
    
    public UID getRdfProperty() {
        return rdfProperty;
    }

    public String getName() {
        return name;
    }

    public TypeModel getType() {
        return type;
    }
    
    @Override
    public String toString(){
        return type + " " + name;
    }

    @Override
    public int compareTo(PropertyModel o) {
        return name.compareTo(o.name);
    }
}
