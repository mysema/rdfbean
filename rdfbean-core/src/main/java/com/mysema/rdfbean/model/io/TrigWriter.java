package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class TrigWriter implements RDFWriter{
    
    private final TurtleWriter writer;

    public TrigWriter(Writer w) {
        writer = new TurtleWriter(w);
    }
    
    public TrigWriter(OutputStream out) {
        writer = new TurtleWriter(out);
    }
    
    @Override
    public void end() {
        writer.end();
    }

    @Override
    public void handle(STMT stmt) {
        writer.handle(stmt);
    }

    @Override
    public void namespace(String prefix, String namespace) {
        writer.namespace(prefix, namespace);
    }

    @Override
    public void start() {
        writer.start();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}
