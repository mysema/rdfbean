package com.mysema.rdfbean.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BindingsTest {
    
    @Test
    public void To_String(){
        Bindings bindings = new Bindings(new Bindings());
        bindings.put("s", new LIT("X"));
        assertEquals("{} {s=\"X\"}", bindings.toString());
    }

}
