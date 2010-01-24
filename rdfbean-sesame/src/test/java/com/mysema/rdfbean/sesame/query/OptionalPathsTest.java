/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * OptionalPathsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OptionalPathsTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public static class Note {
        @Predicate
        String basicForm;        
        @Predicate
        String lemma;        
        @Predicate
        Term term;
        public String getBasicForm() {
            return basicForm;
        }
        public String getLemma() {
            return lemma;
        }
        public Term getTerm() {
            return term;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Term{
        @Predicate
        String meaning;
        public String getMeaning() {
            return meaning;
        }                   
    }
    
    @Test
    public void test() throws StoreException, IOException{
        session = createSession(Note.class, Term.class);
        
        Note note = new Note();
        note.lemma = "a";
        session.save(note);
        
        Note noteVar = Alias.alias(Note.class);
        BooleanBuilder builder = new BooleanBuilder();
        builder.or($(noteVar.getLemma()).eq("a"));
        builder.or($(noteVar.getTerm().getMeaning()).eq("a"));
        assertFalse(session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).isEmpty());               
    }
}
