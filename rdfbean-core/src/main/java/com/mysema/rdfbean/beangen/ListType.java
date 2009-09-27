package com.mysema.rdfbean.beangen;

import java.util.List;

import com.mysema.rdfbean.model.UID;

/**
 * SetType provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ListType extends Type{
    
    private final Type componentType;

    public ListType(Type componentType){
        this(componentType.getRdfType(), componentType);
    }
    
    public ListType(UID rdfType, Type componentType) {
        super(rdfType, List.class);
        this.componentType = componentType;
    }

    @Override
    public String getLocalName(){
        return getSimpleName() + "<" + componentType + ">";
    }
    
}
