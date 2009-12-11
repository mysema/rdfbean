/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Date;
import java.util.Set;

import org.compass.core.Property.Store;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * SessionIndexingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IndexingBlogTest extends AbstractLuceneTest{
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class Article{
        
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
        Set<Tag> tags;
        
        @Predicate
        User author;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        @Predicate
        String firstName, lastName, userName;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Tag{
        @Predicate
        String name;
        
        public Tag(){}
        
        public Tag(String name){
            this.name = name;
        }
    }
    
    @Test
    public void test(){
        Session session = SessionUtil.openSession(luceneRepository, Article.class, User.class, Tag.class);
        
        Tag java = new Tag("java");
        Tag web = new Tag("web");
        Tag dev = new Tag("dev");
        session.saveAll(java, web, dev);
        
        User user = new User();
        user.firstName = "John";
        user.lastName = "Smith";
        user.userName = "johnsmith";
        session.save(user);
        
        Article article = new Article();
        article.title = "Title";
        article.text = "Text";
        article.created = new Date();
    }

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Article.class, User.class, Tag.class);
    }

}
