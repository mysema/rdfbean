package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.ItemDomain;
import com.mysema.rdfbean.model.FOAF;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.util.FileUtils;

public class MemoryStoreTest implements ItemDomain {

    private static final String DATA_DIR = "target/MemoryStoreTest";

    private SessionFactoryImpl sessionFactory;

    @Before
    public void setUp() throws IOException {
        Configuration configuration = new DefaultConfiguration(Item.class);
        MemoryRepository repository = new MemoryRepository();
        if (new File(DATA_DIR).exists()) {
            FileUtils.delete(new File(DATA_DIR));
            new File(DATA_DIR).mkdir();
        }
        repository.setSesameInference(false);
        repository.setDataDirName(DATA_DIR);
        repository.setSources(
                new RDFSource("classpath:/test.ttl", Format.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", Format.RDFXML, FOAF.NS)
                );

        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
    }

    @After
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void test() throws IOException {
        Session session = sessionFactory.openSession();
        RDFBeanTransaction tx = session.beginTransaction();

        Item item = new Item();
        item.setPath("xxx");
        session.save(item);
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        QItem itemVar = QItem.item;
        try {
            assertEquals(1, session.from(itemVar).list(itemVar).size());
            Item i = session.from(itemVar).list(itemVar).get(0);
            assertNotNull(i);
            assertEquals("xxx", i.getPath());

            assertNotNull(session.from(itemVar).where(itemVar.path.eq("xxx")).uniqueResult(itemVar));
        } finally {
            session.close();
        }
    }

}
