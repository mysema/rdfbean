/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Date;
import java.util.Set;

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
        @SearchablePredicate
        String title;
        
        @Predicate
        @SearchablePredicate
        String text;
        
        @Predicate
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
    }
    
    @Test
    public void test(){
        Session session = SessionUtil.openSession(luceneRepository, Article.class, User.class, Tag.class);
    }

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Article.class, User.class, Tag.class);
    }

}
