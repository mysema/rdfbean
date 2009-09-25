package com.mysema.rdfbean.beangen;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;

/**
 * Property provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class PropertyModel implements Comparable<PropertyModel>{

    private final UID rdfProperty;
    
    private final String name;
    
    private final TypeModel type;
    
    private final TypeModel keyType;
    
    private final TypeModel valueType;
    
    public PropertyModel(UID rdfProperty, String name, TypeModel type){
        this(rdfProperty, name, type, null, null);
    }
    
    public PropertyModel(UID rdfProperty, String name, TypeModel type, TypeModel keyType, TypeModel valueType){
        this.rdfProperty = rdfProperty;
        this.name = name;
        this.type = type;
        this.keyType = keyType;
        this.valueType = valueType;
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
    
    
    
    public TypeModel getKeyType() {
        return keyType;
    }

    public TypeModel getValueType() {
        return valueType;
    }

    @Override
    public String toString(){
        return type + " " + name;
    }

    @Override
    public int compareTo(PropertyModel o) {
        return name.compareTo(o.name);
    }

    public PropertyModel merge(PropertyModel other, TypeModel defaultType) {
        if (!other.getType().equals(defaultType)){
            return other;
        }else{
            return this;
        }        
    }
    
}
