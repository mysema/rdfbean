package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;

public class ListContainerTest {

    @ClassMapping
    public static class Example {

        @Id
        public String id;

        @Predicate
        public List<String> list;

    }

    @ClassMapping
    public static class ChildContainer {

        @Id
        public String id;

        @Predicate
        public List<Child> children;

    }

    @ClassMapping
    public static class Child {

        @Id
        public String id;

    }

    @Test
    public void Load_String_List(){
        Session session = SessionUtil.openSession(Example.class);
        Example example = new Example();
        example.list = Arrays.asList("1","2","3","4");
        session.save(example);
        session.clear();

        example = session.findInstances(Example.class).get(0);
        assertEquals(Arrays.asList("1","2","3","4"), example.list);
    }

    @Test
    public void Load_Entity_List(){
        Session session = SessionUtil.openSession(ChildContainer.class, Child.class);
        ChildContainer example = new ChildContainer();
        example.children = Arrays.asList(new Child(), new Child(), new Child());
        session.save(example);
        session.clear();

        example = session.findInstances(ChildContainer.class).get(0);
        assertEquals(3, example.children.size());
    }

}
