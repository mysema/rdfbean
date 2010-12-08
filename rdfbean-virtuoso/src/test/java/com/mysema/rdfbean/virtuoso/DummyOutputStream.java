package com.mysema.rdfbean.virtuoso;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream{
    
    private long length;

    @Override
    public void write(int b) throws IOException {
        length++;
    }
    
    public long getLength() {
        return length;
    }

}
