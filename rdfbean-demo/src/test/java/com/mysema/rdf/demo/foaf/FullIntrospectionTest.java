package com.mysema.rdf.demo.foaf;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.generic.EntityAccess;
import com.mysema.rdf.demo.generic.PropertyAccess;
import com.mysema.rdfbean.model.STMT;

public class FullIntrospectionTest {
    
    private Person person = new Person();

    @Test
    public void testToStatements(){
        EntityAccess<?> accessor = person.getGenericAccess();
        for (STMT stmt : accessor.getStatements()){
            System.out.println(stmt.getPredicate() + " " + stmt.getObject());
        }        
    }
    
    @Test
    public void testWithPropertyAccessor(){
        EntityAccess<?> accessor = person.getGenericAccess();
        for (PropertyAccess<?> prop : accessor.getProperties()){
            System.out.println(prop.getUID());
            if (prop.isSingleValue()){
                Labeled value = accessor.getValue(Labeled.class, prop.getUID());
                if (value != null){
                    System.out.println(value.getLabel());    
                }                
            }else if (prop.isLocalized()){
                // ?!?
            }else{
                for (Labeled value : accessor.getValues(Labeled.class, prop.getUID())){
                    System.out.println(value.getLabel());
                }
            }                        
        }
    }
}
