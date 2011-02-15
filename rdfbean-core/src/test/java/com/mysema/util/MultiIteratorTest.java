package com.mysema.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;


public class MultiIteratorTest {

    @Test
    public void Two_times_three(){
        List<List<String>> strs = new ArrayList<List<String>>();
        strs.add(Arrays.asList("1","2"));
        strs.add(Arrays.asList("1","2","3"));
        MultiIterator<String> iterator = new MultiIterator<String>(strs);
        assertEquals(Arrays.asList("1","2","3","1","2","3"), IteratorAdapter.asList(iterator));
    }
    
    @Test
    public void Three_times_three(){
        List<List<String>> strs = new ArrayList<List<String>>();
        strs.add(Arrays.asList("1","2","3"));
        strs.add(Arrays.asList("1","2","3"));
        MultiIterator<String> iterator = new MultiIterator<String>(strs);
        assertEquals(Arrays.asList("1","2","3","1","2","3","1","2","3"), IteratorAdapter.asList(iterator));
    }
    
}
