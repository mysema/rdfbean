package com.mysema.rdf.demo.foaf;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Document;
import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdfbean.model.UID;

public class HomepageTest {
    
    private final UID workInfoHomepage = new UID("foaf", "workInfoHomepage");
    
    private final UID workplaceHomepage = new UID("foaf", "workplaceHomepage");
    
    private final UID schoolHomepage = new UID("foaf", "schoolHomepage");

    private Person person = new Person();
    
//    @Test
//    public void listAll() {
//        // get all homepage values projected as Document instances
//        Resource<Document> entity = person.getGenericEntity();
//        
//        for (Property<Document> prop : entity.getProperties(workInfoHomepage, workplaceHomepage, schoolHomepage)) {
//            for (Document doc : prop.getReferences()) {
//                System.out.println(doc.getLabel());
//            }
//        }
//        
////        for (Document document : accessor.get.getReferences(workInfoHomepage, workplaceHomepage, schoolHomepage)) {
////            System.out.println(document.getLabel());
////        }
//    }
    
//    @Test
//    public void countHomepages() {
//        
//        Resource<Document> entity = person.getGenericEntity();
//        int count = 0;
//
//        for (Property<Document> prop : entity.getProperties(workInfoHomepage, 
//                workplaceHomepage, schoolHomepage)) {
//            count = count + prop.getValueCount();
//        }
//        
//        System.out.println(count); 
//    }
    
    @Test
    public void addNewHomepage(){
        Document homepage = new Document();
        homepage.setLabel("new homepage");
        person.getGenericEntity().getProperty(workInfoHomepage).add(homepage);
    }
    
    @Test
    public void setHomepage(){
        Document homepage = new Document();
        homepage.setLabel("new homepage");
        person.getGenericEntity().getProperty(workInfoHomepage).setReference(homepage);
    }
    
    @Test
    public void removeHomepage(){
        Document homepage = new Document();
        person.getGenericEntity().getProperty(workInfoHomepage).remove(homepage);
    }
    
    @Test
    public void removeAllHomepages(){
        person.getGenericEntity().getProperty(workInfoHomepage).removeAll();
    }
    
    @Test
    public void nonExistentTest() {
        person.getGenericEntity().getProperty(workInfoHomepage).removeAll();
        System.out.println(person.getGenericEntity()
                .getProperty(workInfoHomepage).getValueCount());
    }
}