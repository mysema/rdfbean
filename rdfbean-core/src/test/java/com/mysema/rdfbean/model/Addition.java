package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class Addition implements RDFConnectionCallback<Void>{
    
    private final STMT[] stmts;
    
    public Addition(STMT... stmts){
        this.stmts = stmts;
    }

    @Override
    public Void execute(RDFConnection connection) throws IOException {
        connection.update(Collections.<STMT>emptySet(), new HashSet<STMT>(Arrays.asList(stmts)));
        return null;
    }

}
