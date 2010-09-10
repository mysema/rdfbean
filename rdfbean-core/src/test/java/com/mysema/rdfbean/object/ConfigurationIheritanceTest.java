package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationIheritanceTest {
    
    public static class Identifiable {
        
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        
        
    }
    
    public static class Category extends Identifiable {
     
        private String label;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
        
        
    }
    
    @Test
    public void MappedClass_Has_IdProperty(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(Identifiable.class).addId("id").addProperties();
        builder.addClass(Category.class).addProperties();
        Configuration configuration = builder.build();    
        
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            assertTrue(mappedClass.getIdProperty() != null);
        }
    }

}
