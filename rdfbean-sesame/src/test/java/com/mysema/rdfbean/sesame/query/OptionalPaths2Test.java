/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Before;
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
public class OptionalPaths2Test extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public static class NoteRevision {
        
        @Predicate
        String lemma;
        
        @Predicate
        Note note;
        
        public String getLemma() {
            return lemma;
        }
        public Note getNote() {
            return note;
        }                
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Note { 
        @Predicate
        Term term;
        
        @Predicate
        NoteRevision latestRevision;
        
        public Term getTerm() {
            return term;
        }

        public NoteRevision getLatestRevision() {
            return latestRevision;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Term{
        
        @Predicate
        String basicForm;
        
        @Predicate
        String meaning;
        
        public String getBasicForm(){
            return basicForm;
        }
        
        public String getMeaning() {
            return meaning;
        }                   
    }
    
    @Before
    public void setUp() throws StoreException{
        session = createSession(NoteRevision.class, Note.class, Term.class);
        
        Note note = new Note();
        session.save(note);        
        NoteRevision rev = new NoteRevision();
        rev.note = note;
        rev.lemma = "X a X";
        session.save(rev);
        
        Term term = new Term();
        term.meaning = "X c X";
        session.save(term);
        Note note2 = new Note();
        note2.term = term;
        session.save(note2);
        NoteRevision rev2 = new NoteRevision();
        rev2.note = note2;
        rev2.lemma = "X b X";
        session.save(rev2);
    }
    
    @Test
    public void test() throws StoreException, IOException{
        NoteRevision noteVar = Alias.alias(NoteRevision.class);
        BooleanBuilder builder = new BooleanBuilder();
        builder.or($(noteVar.getLemma()).contains("a", false));
        builder.or($(noteVar.getNote().getTerm().getMeaning()).contains("a",false));
        assertEquals(1, session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).size());
        System.out.println();
        
        builder = new BooleanBuilder();
        builder.or($(noteVar.getLemma()).isNotNull().and($(noteVar.getLemma()).contains("c",false)));
        builder.or($(noteVar.getNote().getTerm()).isNotNull().and($(noteVar.getNote().getTerm().getMeaning()).contains("c",false)));
        assertEquals(1, session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).size());
        System.out.println();
        
        builder = new BooleanBuilder();
        builder.or($(noteVar.getLemma()).contains("c",false));
        builder.or($(noteVar.getNote().getTerm().getMeaning()).contains("c",false));
        assertEquals(1, session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).size());
        System.out.println();
        
        builder = new BooleanBuilder();
        builder.and($(noteVar.getNote().getLatestRevision()).eq($(noteVar)));
        builder.or($(noteVar.getLemma()).contains("c",false));
        builder.or($(noteVar.getNote().getTerm().getMeaning()).contains("c",false));
        assertFalse(session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).isEmpty());      
        
        builder = new BooleanBuilder();
        builder.or($(noteVar.getNote().getTerm().getBasicForm()).contains("c", false));
        builder.or($(noteVar.getNote().getTerm().getMeaning()).contains("c", false));        
        assertEquals(1, session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).size());
    }
    
}
