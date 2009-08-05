package com.mysema.rdf.demo.foaf;

import java.util.Locale;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.generic.Resource;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

public class NicknameTest {
    
    private final UID nickProp = new UID("foaf", "nick");
    
    private Person person = new Person();
    
    @Test
    public void add(){    
        Resource<?> entity = person.getGenericEntity();
        LIT nickname = new LIT("New nickname");
        entity.getProperty(nickProp).add(nickname);
    }
    
    @Test
    public void set() {
        Resource<?> entity = person.getGenericEntity();
        LIT nickname = new LIT("New nickname");
        entity.getProperty(nickProp).setLiteral(nickname);
    }
    
    @Test
    public void remove() {
        Resource<?> entity = person.getGenericEntity();
        LIT nickname = new LIT("New nickname");
        entity.getProperty(nickProp).remove(nickname);
    }
    
    @Test
    public void removeAll() {
        person.getGenericEntity().getProperty(nickProp).removeAll();
    }
    
    @Test
    public void addLocalized(){        
        Resource<?> entity = person.getGenericEntity();
        LIT nickname = new LIT("New nickname", Locale.ENGLISH);
        entity.getProperty(nickProp).add(nickname);
    }
    
    @Test
    public void countNicknames(){        
        System.out.println(person.getGenericEntity().getProperty(nickProp)
                .getValueCount());
    }

    @Test
    public void singleValueAccess(){
        System.out.println(person.getGenericEntity()
                .getProperty(nickProp).getLiteral().getValue());
    }
    
    @Test
    public void localizedAccess(){        
        System.out.println(person.getGenericEntity()
                .getProperty(nickProp).getLiteral(Locale.ENGLISH));
    }
    
    @Test
    public void nonExistentTest() {
        person.getGenericEntity().getProperty(nickProp).removeAll();
        System.out.println(person.getGenericEntity().getProperty(nickProp)
                .getValueCount());
    }
}