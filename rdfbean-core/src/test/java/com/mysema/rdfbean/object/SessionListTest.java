package com.mysema.rdfbean.object;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;

public class SessionListTest {

    @ClassMapping
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

    @Test
    public void List_populates_Cache(){
        Session session = SessionUtil.openSession(Example.class);
        Example example1 = new Example();
        Example example2 = new Example();
        session.saveAll(example1, example2);

        session.clear();
        Example var = Alias.alias(Example.class);
        List<Example> examples = session.from($(var)).list($(var));
        assertTrue(examples.contains(session.getById(example1.id, Example.class)));
        assertTrue(examples.contains(session.getById(example2.id, Example.class)));
    }

}
