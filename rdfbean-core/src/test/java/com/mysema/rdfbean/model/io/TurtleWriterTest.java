package com.mysema.rdfbean.model.io;

import java.io.Writer;

import com.mysema.rdfbean.model.Format;

import junit.framework.Assert;


public class TurtleWriterTest extends AbstractWriterTest{

    @Override
    protected RDFWriter createWriter(Writer w) {
        return WriterUtils.createWriter(Format.TURTLE, w);
    }
    
    @Override
    protected void validate(String str) {
        Assert.assertTrue(str.contains(" ; "));
        Assert.assertTrue(str.contains(" , "));
    }

}
