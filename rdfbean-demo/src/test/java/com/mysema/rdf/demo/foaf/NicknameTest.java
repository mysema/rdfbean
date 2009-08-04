package com.mysema.rdf.demo.foaf;

import java.util.Locale;

import org.junit.Test;

import com.mysema.rdf.demo.foaf.domain.Person;
import com.mysema.rdf.demo.generic.EntityAccess;
import com.mysema.rdfbean.model.UID;

public class NicknameTest {
    
    private final UID nickProp = new UID("foaf", "nick");
    
    private Person person = new Person();
    
    // Entityn tietojen tulostus
    
    @Test
    public void add(){    
        EntityAccess<?> accessor = person.getGenericAccess();
        accessor.addValue(nickProp, "New nickname");
    }
    
    @Test
    public void addLocalized(){        
        EntityAccess<?> accessor = person.getGenericAccess();
        accessor.addValue(nickProp, Locale.ENGLISH, "New nickname");
    }
    
    @Test
    public void singleValueAccess(){
        EntityAccess<?> accessor = person.getGenericAccess();
        String nick = accessor.getValue(String.class, nickProp);
        System.out.println(nick);
    }
    
    @Test
    public void localizedAccess(){
        EntityAccess<?> accessor = person.getGenericAccess();
//      String nick = person.getValueAccessor(nickProp).get(String.class, Locale.ENGLISH);
        String nick = accessor.getValue(String.class, Locale.ENGLISH, nickProp);
        System.out.println(nick);
    }
}
