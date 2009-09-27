package com.mysema.rdfbean.beangen;

import java.util.Set;

import com.mysema.rdfbean.model.UID;

/**
 * SetType provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SetType extends Type{
    
    private final Type componentType;

    public SetType(Type componentType){
        this(componentType.getRdfType(), componentType);
    }
    
    public SetType(UID rdfType, Type componentType) {
        super(rdfType, Set.class);
        this.componentType = componentType;
    }

    @Override
    public String getLocalName(){
        return getSimpleName() + "<" + componentType + ">";
    }
    
}
