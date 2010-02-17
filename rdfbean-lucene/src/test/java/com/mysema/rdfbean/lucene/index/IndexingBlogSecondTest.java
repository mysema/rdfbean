/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.index;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.compass.core.CompassHits;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.lucene.Constants;
import com.mysema.rdfbean.lucene.LuceneQuery;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchablePredicate;
import com.mysema.rdfbean.lucene.SearchableText;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * IndexingBlog2Test provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IndexingBlogSecondTest  extends AbstractIndexTest{

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Article.class, Tag.class);
    }
    
    @Test
    public void searchByTag() throws IOException{
        Tag java = new Tag("java");
        Tag web = new Tag("web");
        Tag dev = new Tag("dev");
        session.saveAll(java, web, dev);
        
        Article article = new Article();
        article.title = "This is the title";
        article.tags = Arrays.asList(java, web, dev);
        session.save(article);        
        session.clear();
        
        CompassHits hits = session.createQuery(Constants.COMPASSQUERY).term("text", "title").hits();
        displayAndClose(hits);
                
        // article search
        
        for (Tag tag : article.tags){
            LuceneQuery query = session.createQuery(Constants.LUCENEQUERY);
            assertFalse(query.query(tag.getName()).list(Article.class).isEmpty());    
        }
        
        LuceneQuery query = session.createQuery(Constants.LUCENEQUERY);
        assertTrue(query.query("XXX").list(Article.class).isEmpty());        
    }
    
    public void setUp() throws IOException, InterruptedException{
        super.setUp();
        session = SessionUtil.openSession(repository, Article.class, Tag.class);
    }
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class Article{
        
        @Id
        String id;
        
        // indexed into ":tagged" fields
        @Predicate(ln="tagged")
        @SearchablePredicate(store=Store.NO,index=Index.ANALYZED)
        Collection<Tag> tags;
        
        @Predicate
        @SearchablePredicate
        @SearchableText
        String title;
        
    }    
    
    @ClassMapping(ns=TEST.NS)
    public static class Tag{
        @Id(IDType.URI)
        UID id;
        
        protected Tag(){}
        
        public Tag(String name){
            this.id = new UID("tag:", name);
        }
        
        public String getName(){
            return id.getLocalName();
        }
    }

}
