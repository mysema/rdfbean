package com.mysema.rdfbean.beangen;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.mysema.rdfbean.beangen.JavaBeanExporter;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class JavaBeanExporterTest {

    @Test
    public void testExport() throws IOException {
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/blog.owl",Format.RDFXML, "http://www.mysema.com/semantics/blog/#"),
                new RDFSource("classpath:/dc.rdf", Format.RDFXML, "http://purl.org/dc/elements/1.1/"),
                new RDFSource("classpath:/demo.owl",Format.RDFXML, "http://www.mysema.com/rdfbean/demo"),
                new RDFSource("classpath:/wine.owl",Format.RDFXML, "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#"));                
        
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.addPackages(RDFSClass.class.getPackage(), OWLClass.class.getPackage());
        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        Session session = sessionFactory.openSession();
        
        try{
            JavaBeanExporter exporter = new JavaBeanExporter(true);
            exporter.addPackage("http://www.mysema.com/semantics/blog/#", "com.mysema.blog");
            exporter.addPackage("http://purl.org/dc/elements/1.1/", "com.mysema.dc");
            exporter.addPackage("http://www.mysema.com/rdfbean/demo#", "com.mysema.demo");
            exporter.addPackage("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#", "com.mysema.wine");
            exporter.export(session, new File("target/export"));
        }finally{
            try {
                session.close();
            }finally{
                sessionFactory.close();
            }
        }
    }

}
