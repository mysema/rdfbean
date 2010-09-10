package com.mysema.rdfbean.object;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.MiniRepository;

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

        private Set<Category> children;
        
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Set<Category> getChildren() {
            return children;
        }

        public void setChildren(Set<Category> children) {
            this.children = children;
        }
        
        
    }
    
    private Configuration configuration;
    
    @Before
    public void setUp(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(Identifiable.class).addId("id").addProperties();
        builder.addClass(Category.class).addProperties();
        configuration = builder.build();
    }
    
    @Test
    public void MappedClass_Has_IdProperty(){
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            assertTrue(mappedClass.getIdProperty() != null);
        }
    }
    
    @Test
    public void Save() throws IOException{
        Session session = SessionUtil.openSession(new MiniRepository(), Collections.<Locale>emptySet(), configuration);
        Category category = new Category();
        category.label = "X";
        category.children = Collections.singleton(new Category());
        session.save(category);
        session.flush();
        session.close();
    }

}
