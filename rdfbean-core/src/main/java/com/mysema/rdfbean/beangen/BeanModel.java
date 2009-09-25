package com.mysema.rdfbean.beangen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.mysema.rdfbean.model.UID;

/**
 * BeanModel provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanModel extends TypeModel{
    
    private final Set<PropertyModel> properties = new TreeSet<PropertyModel>();

    private final List<TypeModel> superTypes = new ArrayList<TypeModel>();
    
    public BeanModel(UID rdfType, String packageName, String simpleName){
        super(rdfType, packageName, simpleName);
    }

    public Set<PropertyModel> getProperties() {
        return properties;
    }
    
    public void addProperty(PropertyModel property){
        properties.add(property);
    }
    
    public List<TypeModel> getSuperTypes() {
        return superTypes;
    }

    public void addSuperType(TypeModel type) {
        superTypes.add(type);
        
    }

}
