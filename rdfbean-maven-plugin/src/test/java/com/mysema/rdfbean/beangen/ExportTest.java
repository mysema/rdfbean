package com.mysema.rdfbean.beangen;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExportTest extends AbstractExportTest{

    @Test
    public void testExport() throws IOException {
        JavaBeanExporter exporter = new JavaBeanExporter(true);
        exporter.addPackage("http://www.mysema.com/semantics/blog/#", "com.mysema.blog");
        exporter.addPackage("http://purl.org/dc/elements/1.1/", "com.mysema.dc");
        exporter.addPackage("http://www.mysema.com/rdfbean/demo#", "com.mysema.demo");
        exporter.addPackage("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#", "com.mysema.wine");
        exporter.export(session, new File("target/export"));
    }

}
