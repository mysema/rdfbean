package com.mysema.rdf.demo.foaf;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Person;

public class FullIntrospectionTest {
    
    private Person person = new Person();

//    @Test
//    public void testToStatements(){
//        
//        Entity<?> accessor = person.getGenericEntity();
//        
//        for (STMT stmt : accessor.getStatements()){
//            System.out.println(stmt.getPredicate() + " " + stmt.getObject());
//        }        
//    }
    
    @Test
    public void testWithPropertyAccessor(){
//        Resource<Object> entity = person.getGenericEntity();
//        
//        for (Property<Object> prop : entity.getProperties()){
//            System.out.println(prop.getId());
//            for (Value<Object> value : prop.getValues()) {
//                if (value.isLiteral()) {
//                    LIT lit = value.getLiteral();
//                    System.out.println(lit.getLang() + ":" + lit.getValue());
//                }
//                
//                if (value.isReference()) {
//                    Object obj = value.getReference();
//                    if (obj instanceof Document) {
//                        System.out.println("Document:" + 
//                                ((Document) obj).getLabel());
//                    }
//                    else {
//                        System.out.println(obj.getClass().getCanonicalName());
//                    }
//                }
//            }            
//        }
    }
}