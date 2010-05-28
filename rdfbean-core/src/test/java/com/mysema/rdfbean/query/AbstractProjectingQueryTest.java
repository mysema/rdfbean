package com.mysema.rdfbean.query;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniDialect;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

public class AbstractProjectingQueryTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity{
	
	@Id(IDType.LOCAL)
	String id;
    }
    
    @Test
    public void test(){
	Session session = SessionUtil.openSession(Entity.class);
	MiniDialect dialect = new MiniDialect();
	PathBuilder<Entity> entity = new PathBuilder<Entity>(Entity.class,"entity");
	DummyQuery query = new DummyQuery(new DefaultConfiguration(Entity.class), dialect,session).from(entity);
	query.count();
	query.countDistinct();
	query.iterate(entity);
	query.iterateDistinct(entity);
	query.list(entity);
	query.listDistinct(entity);
	query.map(entity, entity);
	query.uniqueResult(entity);
	
	query.listResults(entity);
    }

}
