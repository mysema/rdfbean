package com.mysema.rdfbean.model.io;

import java.io.Writer;


public class TurtleWriterTest extends AbstractWriterTest{

    @Override
    protected RDFWriter createWriter(Writer w) {
        return WriterUtils.createWriter(Format.TURTLE, w);
    }

}
