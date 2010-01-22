package com.mysema.rdfbean.sesame.query;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.BooleanBuilder;
import static com.mysema.query.alias.Alias.*;
import static org.junit.Assert.*;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * OptionalPathsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OptionalPaths2Test extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS, ln="NoteRevision_OptPath")
    public static class NoteRevision {
        @Predicate
        String basicForm;
        
        @Predicate
        String lemma;
        
        @Predicate
        Note note;
        
        public String getBasicForm() {
            return basicForm;
        }
        public String getLemma() {
            return lemma;
        }
        public Note getNote() {
            return note;
        }                
    }
    
    @ClassMapping(ns=TEST.NS, ln="Note_OptPath")
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
    
    @ClassMapping(ns=TEST.NS, ln="Term_OptPath")
    public static class Term{
        @Predicate
        String meaning;
        
        public String getMeaning() {
            return meaning;
        }                   
    }
    
    private Session session;
    
    @After
    public void tearDown() throws IOException{
        if (session != null) session.close();
    }
    
    @Test
    public void test() throws StoreException, IOException{
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
        
        NoteRevision noteVar = Alias.alias(NoteRevision.class);
        BooleanBuilder builder = new BooleanBuilder();
        builder.or($(noteVar.getLemma()).contains("a", false));
        builder.or($(noteVar.getNote().getTerm().getMeaning()).contains("a",false));
        assertEquals(1, session.from($(noteVar)).where(builder.getValue()).list($(noteVar)).size());
        System.out.println();
        
//      SELECT DISTINCT noteRevision
//      FROM {noteRevision} rdf:type test:NoteRevision.
//        OPTIONAL ( {noteRevision} test:lemma {_var_c} ).
//        OPTIONAL ( {noteRevision} test:note {_var_f} . {_var_f} test:term {_var_h} . {_var_h} test:meaning {_var_j} ).
//      WHERE <functions:stringContainsIc>( {_var_c},"a"^^xsd:string ) OR
//        <functions:stringContainsIc>( {_var_j},"a"^^xsd:string )
        
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
    }
    

    
    
}
