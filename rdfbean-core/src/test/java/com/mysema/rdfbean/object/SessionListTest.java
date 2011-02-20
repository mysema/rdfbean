package com.mysema.rdfbean.object;

import java.util.List;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import static com.mysema.query.alias.Alias.*;
import static org.junit.Assert.*;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;

public class SessionListTest {

    @ClassMapping(ns=TEST.NS)
    public static class Example {
        
        @Id
        public String id;
    }
    
    @Test
    public void List_uses_Cache(){
        Session session = SessionUtil.openSession(Example.class);
        Example example1 = new Example();
        Example example2 = new Example();
        session.saveAll(example1, example2);
        
        Example var = Alias.alias(Example.class);
        List<Example> examples = session.from($(var)).list($(var));
        assertTrue(examples.contains(example1));
        assertTrue(examples.contains(example2));
    }
    
}
