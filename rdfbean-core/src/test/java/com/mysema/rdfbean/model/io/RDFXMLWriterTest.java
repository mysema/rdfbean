package com.mysema.rdfbean.model.io;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.Format;

public class RDFXMLWriterTest extends AbstractWriterTest {

    @Override
    protected RDFWriter createWriter(Writer w) {
        Map<String, String> prefixes = new HashMap<String, String>(Namespaces.DEFAULT);
        prefixes.put(DC.NS, "dc");
        return WriterUtils.createWriter(Format.RDFXML, w, prefixes);
    }

    @Override
    protected void validate(String str) {
        Assert.assertTrue(str.contains("&amp;"));
    }

}
