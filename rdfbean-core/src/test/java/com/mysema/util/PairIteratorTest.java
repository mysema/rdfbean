package com.mysema.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;

public class PairIteratorTest {

    @Test
    public void Two_times_three() {
        List<String> strs1 = Arrays.asList("1", "2");
        List<String> strs2 = Arrays.asList("1", "2", "3");
        PairIterator<String> iterator = new PairIterator<String>(strs1, strs2);
        assertEquals(Arrays.asList("1", "2", "3", "1", "2", "3"), IteratorAdapter.asList(iterator));
    }

    @Test
    public void Three_times_three() {
        List<String> strs1 = Arrays.asList("1", "2", "3");
        List<String> strs2 = Arrays.asList("1", "2", "3");
        PairIterator<String> iterator = new PairIterator<String>(strs1, strs2);
        assertEquals(Arrays.asList("1", "2", "3", "1", "2", "3", "1", "2", "3"), IteratorAdapter.asList(iterator));
    }

}
