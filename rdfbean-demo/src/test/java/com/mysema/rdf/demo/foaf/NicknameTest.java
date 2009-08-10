package com.mysema.rdf.demo.foaf;

import java.util.Locale;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.foaf.v1.ID;
import com.mysema.rdf.demo.foaf.v1.MLiteral;
import com.mysema.rdf.demo.foaf.v1.MLocalizedLiteral;
import com.mysema.rdf.demo.foaf.v1.MResource;
import com.mysema.rdf.demo.foaf.v1.Person1;
import com.mysema.rdf.demo.generic.Resource;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

public class NicknameTest {
    
    private final UID nickProp = new UID("foaf", "nick");
    private final ID nickId = new ID("nick");
    private Person person = new Person();
    
    private MResource mresource = new Person1().getResource();
    
    <T> T getEntity(Class<T> clazz) {
        return null;
    }
    
    @Test
    public void add(){    
        Resource<?> entity = person.getGenericEntity();
        LIT nickname = new LIT("New nickname");
        entity.getProperty(nickProp).add(nickname);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void add1(){    
        MLiteral<String> nickname = getEntity(MLiteral.class);
        nickname.setValue("New nickname");
        mresource.getProperty(nickId).addValue(nickname);
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
    public void addLocalized1(){        
        MLocalizedLiteral nickname = getEntity(MLocalizedLiteral.class);
        nickname.setValue("New nickname 1");
        nickname.setLocale(Locale.ENGLISH);
        mresource.getProperty(nickId).addValue(nickname);
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