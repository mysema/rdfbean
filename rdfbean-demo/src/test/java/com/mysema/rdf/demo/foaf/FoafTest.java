package com.mysema.rdf.demo.foaf;

import static org.junit.Assert.assertEquals;

import org.springframework.beans.PropertyAccessor;

import com.mysema.rdf.demo.foaf.domain.Document;
import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.generic.EntityAccess;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.rdfs.RDFProperty;

public class FoafTest {

    public void dummyTest() {

//        Document workInfo = new Document();
//
//        Person person = new Person();
//        person.setFirstName("Foo");
//        person.setSurname("Bar");
//        
//        EntityAccessor<RefType> accessor = person.getGenericAccessor(); // @GenericAccessor(includeMappedProperties=true, inverse=false)
//        //accessor.getBeans(); // -> List<Object> ???
//        
//        RDFProperty property = new RDFProperty(new UID("...", "workInfoHomepage"));
//        UID propid = (UID) property.getId();
//        accessor.put(propid, collection(workInfo)); // ???
//
//        PropertyAccessor propaccess = accessor.getPropertyAccessor(propid);
//        propaccess.exists();
//        propaccess.isSingleton();
//        propaccess.isMultivalue();
//        propaccess.size();
//        Class type = propaccess.getTargetType();
//        propaccess.isLiteral();
//        propaccess.isReference();
//        propaccess.isMappedProperty(); // in which instance/class?!?
//        propaccess.isList(); // rdf:List
//        propaccess.isContainer(); // rdf:Seq, rdf:Alt, rdf:Bag
//        propaccess.setValue(workInfo); // ???
//        propaccess.getValue(); // Object... Iterable/Iterator?
//        propaccess.getReferenceValue(); // <RefValue>
//        // Is value/referenceValue collection or single object - separate getters/setters for single/multi access?
//        
// 
//
//        
//        assertEquals("Foo", person.getFirstName());
//        assertEquals("Bar", person.getSurname());
//
//        assertEquals(workInfo, accessor.get(Document.class, person,
//            "foaf:workInfoHomepage"));
//        
//        assertEquals("geek", accessor.get(person,
//            "foaf:geekCode"));
    }
}
