package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

public class ConstructorVisitorTest {

    @SuppressWarnings("unused")
    public static class Entity {
        
        private final String firstName, lastName;
        
        public Entity(String firstName, String lastName){            
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        public Entity(String firstName){            
            this.firstName = firstName;
            this.lastName = null;
        }
        
    }
    
    @SuppressWarnings("unused")
    public static class Entity2 {
        
        private long revision;
        
        private String text;
        
        private DateTime created;
        
        public Entity2(){}
        
        public Entity2(long rev, String t, DateTime c){
            revision = rev;
            text = t;
            created = c;
        }
        
    }
    
    @SuppressWarnings("unused")
    public static class Entity3 {
        
        private long revision;
        
        private String text;
        
        private DateTime created;
        
        public Entity3(){}
        
        public Entity3(long revision, String text, DateTime created){
            this.revision = revision;
            this.text = text;
            this.created = created;
        }
        
    }
    
    
    
    @Test
    public void Parse_Entity() throws IOException{
        ClassReader cr = new ClassReader(getResource(Entity.class));
        ConstructorVisitor visitor = new ConstructorVisitor();
        cr.accept(visitor, 0);
        
        assertEquals(2, visitor.getConstructors().size());
        assertEquals(Arrays.asList("firstName", "lastName"), visitor.getConstructors().get(0));
        assertEquals(Arrays.asList("firstName"), visitor.getConstructors().get(1));
    }
    
    @Ignore
    @Test
    public void Parse_Entity2() throws IOException{
        ClassReader cr = new ClassReader(getResource(Entity2.class));
        ConstructorVisitor visitor = new ConstructorVisitor();
        cr.accept(visitor, 0);
        
        assertEquals(1, visitor.getConstructors().size());
        assertEquals(Arrays.asList("rev", "t", "c"), visitor.getConstructors().get(0));
    }
    
    @Ignore
    @Test
    public void Parse_Entity3() throws IOException{
        ClassReader cr = new ClassReader(getResource(Entity3.class));
        ConstructorVisitor visitor = new ConstructorVisitor();
        cr.accept(visitor, 0);
        
        assertEquals(1, visitor.getConstructors().size());
        assertEquals(Arrays.asList("revision", "text", "created"), visitor.getConstructors().get(0));
    }
    
    private InputStream getResource(Class<?> cl){
        return cl.getClassLoader().getResourceAsStream(cl.getName().replace('.', '/') + ".class");
    }
}
