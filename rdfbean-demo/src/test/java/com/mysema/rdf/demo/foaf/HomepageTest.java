package com.mysema.rdf.demo.foaf;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Document;
import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.generic.EntityAccess;
import com.mysema.rdfbean.model.UID;

public class HomepageTest {
    
    private final UID workInfoHomepage = new UID("foaf", "workInfoHomepage");
    
    private final UID workplaceHomepage = new UID("foaf", "workplaceHomepage");
    
    private final UID schoolHomepage = new UID("foaf", "schoolHomepage");

    @Test
    public void listAll() {
        // get all homepage values projected as Document instances
        Person person = new Person();        
        EntityAccess<?> accessor = person.getGenericAccess();         
        for (Document document : accessor.getValues(Document.class, workInfoHomepage, workplaceHomepage, schoolHomepage)) {
            System.out.println(document.getLabel());
        }
    }
    
    @Test
    public void addNewHomepage(){
        Person person = new Person();
        Document homepage = new Document();
        homepage.setLabel("new homepage");
        person.getGenericAccess().addValue(workInfoHomepage, homepage);
    }
    
    @Test
    public void removeHomepage(){
        Person person = new Person();
        Document homepage = new Document();
        person.getGenericAccess().removeValue(workInfoHomepage, homepage);
    }
}
