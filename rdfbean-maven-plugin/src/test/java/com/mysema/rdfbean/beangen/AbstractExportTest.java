package com.mysema.rdfbean.beangen;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.sesame.MemoryRepository;

public abstract class AbstractExportTest {

    private static SessionFactoryImpl sessionFactory;

    protected static Session session;

    @BeforeClass
    public static void setUpClass() {
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/blog.owl", Format.RDFXML, "http://www.mysema.com/semantics/blog/#"),
                new RDFSource("classpath:/dc.rdf", Format.RDFXML, "http://purl.org/dc/elements/1.1/"),
                new RDFSource("classpath:/demo.owl", Format.RDFXML, "http://www.mysema.com/rdfbean/demo"),
                new RDFSource("classpath:/wine.owl", Format.RDFXML, "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#"));

        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.addPackages(RDFSClass.class.getPackage(), OWLClass.class.getPackage());

        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        session = sessionFactory.openSession();
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        if (session != null) {
            session.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

}
