package com.mysema.rdfbean.lucene.store;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.mysema.query.types.expr.EBoolean;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchablePredicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * UpdateTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class UpdateTest extends AbstractStoreTest{

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Article.class);
    }
    
    public void setUp() throws IOException, InterruptedException{
        super.setUp();
        session = SessionUtil.openSession(repository, Article.class);
    }
    
    @Test
    public void titlePersistence(){
        Article article = new Article();
        article.title = "Test";
        session.save(article);
        session.clear();        
        assertTrue(query(QArticle.article.title.eq("Test")) != null);
        
        article.title = "Other";
        session.save(article);
        session.clear();               
        assertTrue(query(QArticle.article.title.eq("Other")) != null);
        assertTrue(query(QArticle.article.title.eq("Test")) == null);
        
        article.title = null;
        session.save(article);
        session.clear();        
        EBoolean notNull = QArticle.article.title.isNotNull();
        assertTrue(query(notNull.and(QArticle.article.title.eq("Test"))) == null);
        assertTrue(query(notNull.and(QArticle.article.title.eq("Other"))) == null);
        assertTrue(query(QArticle.article.title.isNull()) != null);
    }
    
    @Test
    public void titlesPersistence(){
        Article article = new Article();
        article.titles.add("Test");
        session.save(article);
        session.clear();               
        assertTrue(query(QArticle.article.titles.contains("Test")) != null);
        
        article.titles.clear();
        article.titles.add("Other");
        session.save(article);
        session.clear();               
        assertTrue(query(QArticle.article.titles.contains("Other")) != null);
        assertTrue(query(QArticle.article.titles.contains("Test")) == null);
        
        article.titles.clear();        
        session.save(article);
        session.clear();        
        assertTrue(query(QArticle.article.titles.contains("Test")) == null);
        assertTrue(query(QArticle.article.titles.contains("Other")) == null);
        assertTrue(query(QArticle.article.titles.isEmpty()) != null);
        
        article.titles.add("1");
        article.titles.add("2");
        session.save(article);
        session.clear();        
        assertTrue(query(QArticle.article.titles.contains("1")) != null);
        assertTrue(query(QArticle.article.titles.contains("2")) != null);
    }
    
    private Article query(EBoolean cond){
        return session.from(QArticle.article).where(cond).uniqueResult(QArticle.article);
    }
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class Article{
     
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        @SearchablePredicate
        String title;
                
        @Predicate
        @SearchablePredicate
        Set<String> titles = new HashSet<String>();
        
        public String getTitle() {
            return title;
        }

        public Set<String> getTitles(){
            return titles;
        }
        
        
    }
}
