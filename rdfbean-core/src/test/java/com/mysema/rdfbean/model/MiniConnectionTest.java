package com.mysema.rdfbean.model;

import java.util.Collections;

import org.junit.Test;

public class MiniConnectionTest {


    @Test
    public void Update_with_nulls(){
        RDFConnection conn = new MiniRepository().openConnection();
        conn.update(Collections.<STMT>emptySet(), null);
        conn.update(null, Collections.<STMT>emptySet());
        conn.update(null, null);
    }

}
