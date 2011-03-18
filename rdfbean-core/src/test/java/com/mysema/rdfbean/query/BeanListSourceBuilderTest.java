package com.mysema.rdfbean.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;


public class BeanListSourceBuilderTest implements EntityDomain{

    private SessionFactoryImpl sessionFactory;

    @Before
    public void setUp(){
        Configuration configuration = new DefaultConfiguration(TEST.NS, Entity.class);
        Repository repository = new MiniRepository();
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();

        Session session = sessionFactory.openSession();
        for (int i = 0; i < 10; i++){
            session.save(new Entity());
        }
        session.close();
    }

    @Test
    public void Create(){
        QEntity entity = QEntity.entity;
        BeanListSource<Entity> source = new BeanListSourceBuilder(sessionFactory)
            .from(entity).where(entity.text.isNull())
            .orderBy(entity.text.asc())
            .list(entity);
        assertNotNull(source.getResult(0));
        assertFalse(source.getResults(0, 5).isEmpty());
        assertFalse(source.getResults(5, 10).isEmpty());
    }

}
