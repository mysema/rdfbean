/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.load;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.domains.LoadDomain;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.sesame.MemoryRepository;
import com.mysema.rdfbean.sesame.NativeRepository;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.util.FileUtils;

public class LoadTest extends SessionTestBase implements LoadDomain {

    private StringWriter writer = new StringWriter();

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new DefaultConfiguration(Revision.class, Entity.class, Document.class);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.delete(new File("target/native"));
    }

    @Test
    @Ignore
    public void test() throws IOException {
        loadTest("MiniRepository", new MiniRepository());

        // Sesame repositories
        loadTest("MemoryStore, local inference", new MemoryRepository(null, false));
        loadTest("MemoryStore, local inference, synced", new MemoryRepository(new File("target/mem"), 10, false));
        loadTest("NativeStore, local inference", new NativeRepository(new File("target/native"), false));

        // loadTest("MemoryStore, sesame inference", new MemoryRepository(null,
        // true));
        // loadTest("MemoryStore, sesame inferencem, synced", new
        // MemoryRepository(new File("target/mem"), 10, true));
        // loadTest("NativeStore, sesame inference", new NativeRepository(new
        // File("target/native"), true));

        System.out.println(writer);
    }

    private void loadTest(String label, Repository repository) throws IOException {
        writer.write("testing " + label + "\n");

        // if (repository instanceof SesameRepository){
        // ((SesameRepository)repository).setOntology(ontology);
        // }

        for (int size : Arrays.asList(10, 50, 100, 500, 1000, 5000, 10000, 50000)) {
            SessionFactoryImpl sessionFactory = new SessionFactoryImpl(Locale.ENGLISH);
            sessionFactory.setConfiguration(configuration);
            sessionFactory.setRepository(repository);
            sessionFactory.initialize();

            Session localSession = sessionFactory.openSession();
            try {
                loadTest(localSession, size);
            } finally {
                localSession.close();
                sessionFactory.close();
                FileUtils.delete(new File("target/mem"));
                FileUtils.delete(new File("target/native"));
            }
        }
        writer.write("\n");
    }

    private void loadTest(Session session, int size) {
        session.setFlushMode(FlushMode.MANUAL);
        List<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < size; i++) {
            Document document = new Document();
            document.text = UUID.randomUUID().toString();
            objects.add(document);

            Entity entity = new Entity();
            entity.document = document;
            entity.text = UUID.randomUUID().toString();
            objects.add(entity);

            for (int created : Arrays.asList(1, 2, 3, 4, 5, 6)) {
                Revision rev = new Revision();
                rev.svnRevision = 1;
                rev.revisionOf = entity;
                rev.created = created;
                objects.add(rev);
            }
        }

        long t1 = System.currentTimeMillis();
        for (Object o : objects) {
            session.save(o);
        }
        long t2 = System.currentTimeMillis();

        session.flush();
        long t3 = System.currentTimeMillis();

        session.clear();
        session.findInstances(Document.class);
        session.findInstances(Entity.class);
        session.findInstances(Revision.class);
        long t4 = System.currentTimeMillis();

        // size, save time, flush time, load time
        writer.write(";" + objects.size() + ";" + (t2 - t1) + ";" + (t3 - t2) + ";" + (t4 - t3) + "\n");
    }

}
