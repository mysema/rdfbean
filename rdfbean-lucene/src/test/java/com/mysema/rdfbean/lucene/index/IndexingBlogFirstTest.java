/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.compass.core.Property.Store;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.lucene.LuceneQuery;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchableComponent;
import com.mysema.rdfbean.lucene.SearchablePredicate;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * SessionIndexingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IndexingBlogFirstTest extends AbstractIndexTest{
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class Article{
        // rdf:type, test:title, test:text, test:created, test:author
        // test:tagged
        
        @Predicate
        @SearchablePredicate(text=true, store=Store.YES)
        String title;
        
        @Predicate
        @SearchablePredicate(text=true)
        String text;
        
        @Predicate
        @SearchablePredicate
        Date created;
        
        @Predicate(ln="tagged")
        @SearchableComponent
        Collection<Tag> tags;
        
        @Predicate
        @SearchablePredicate
        User author;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        @Predicate
        String firstName, lastName, userName;
    }
    
    @Searchable(embeddedOnly=true)
    @ClassMapping(ns=TEST.NS)
    public static class Tag{
        @Predicate
        @SearchablePredicate(text=true)
        String name;
        
        public Tag(){}
        
        public Tag(String name){
            this.name = name;
        }
    }
    
    @Test
    public void basicSearch() throws IOException{
        Session session = SessionUtil.openSession(repository, Article.class, User.class, Tag.class);
                
        User user = new User();
        user.firstName = "John";
        user.lastName = "Smith";
        user.userName = "johnsmith";
        session.save(user);
        
        Article article = new Article();
        article.title = "A very interesting article";
        article.text = "J2EE is a very good platform for web applications";
        article.created = new Date();
        article.author = user;
        session.save(article);
        
        article = new Article();
        article.title = "Other article";
        article.text = "Python is a very good dynamic language";
        article.created = new Date();
        article.author = user;
        session.save(article);
        session.clear();
        
        // article search
        LuceneQuery query = session.createQuery(LuceneQuery.class);
        assertFalse(query.query("j2ee").list(Article.class).isEmpty());
        
        query = session.createQuery(LuceneQuery.class);
        assertTrue(query.query("dot").list(Article.class).isEmpty());
        
        query = session.createQuery(LuceneQuery.class);
        assertEquals(2, query.query("good").list(Article.class).size());
        
        session.close();
        
    }
    
    @Test
    public void searchByTag() throws IOException{
        // TODO : this should also work without manual flush!
        Session session = SessionUtil.openSession(repository, Article.class, User.class, Tag.class);
        session.setFlushMode(FlushMode.MANUAL);
        
        Tag java = new Tag("java");
        Tag web = new Tag("web");
        Tag dev = new Tag("dev");
        session.saveAll(java, web, dev);
        
        Article article = new Article();
        article.tags = Arrays.asList(java, web, dev);
        session.save(article);        
        session.flush();
        session.clear();
        
        // article search
        
        for (Tag tag : article.tags){
            LuceneQuery query = session.createQuery(LuceneQuery.class);
            assertFalse(query.query(tag.name).list(Article.class).isEmpty());    
        }
        
        LuceneQuery query = session.createQuery(LuceneQuery.class);
        assertTrue(query.query("XXX").list(Article.class).isEmpty());
        
        session.close();
    }

    @Test
    @Ignore
    public void tagPersistence() throws IOException{
        Session session = SessionUtil.openSession(repository, Article.class, User.class, Tag.class);
        Tag java = new Tag("java");
        Tag web = new Tag("web");
        Tag dev = new Tag("dev");
        session.saveAll(java, web, dev);
        
        for (Tag tag : Arrays.asList(java, web, dev)){
            LuceneQuery query = session.createQuery(LuceneQuery.class);
            // tags are only saved as components
            assertTrue("Tags should only be saved in embedded form", query.query(tag.name).list(Tag.class).isEmpty());    
        }
        
        session.close();
    }
    
    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Article.class, User.class, Tag.class);
    }

}