package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.Writer;

import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

public class NTriplesWriter implements RDFWriter{

    private final Writer writer;
    
    public NTriplesWriter(Writer writer) {
        this.writer = writer;        
    }
    
    @Override
    public void begin() {}

    @Override
    public void end() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    @Override
    public void handle(STMT stmt) {
        try {
            writer.append(NTriplesUtil.toString(stmt.getSubject()));
            writer.append(" ");
            writer.append(NTriplesUtil.toString(stmt.getPredicate()));
            writer.append(" ");
            writer.append(NTriplesUtil.toString(stmt.getObject()));
            writer.append(" .\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }    
    }

}
