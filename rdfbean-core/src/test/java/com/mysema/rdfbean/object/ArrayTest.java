package com.mysema.rdfbean.object;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.MiniRepository;

@ClassMapping
public class ArrayTest {

    @Id
    public String id;

    @Predicate
    public String[] array;

    @Predicate
    public int[] numbers;

    @Test
    public void Array() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, ArrayTest.class);

        ArrayTest test = new ArrayTest();
        test.array = new String[] { "a", "b", "c" };
        test.numbers = new int[] { 1, 2, 3 };
        session.save(test);
        session.clear();

        ArrayTest test2 = session.get(ArrayTest.class, new LID(test.id));
        assertEquals(test.id, test2.id);
        assertArrayEquals(test.array, test2.array);
        assertArrayEquals(test.numbers, test2.numbers);

    }

}
