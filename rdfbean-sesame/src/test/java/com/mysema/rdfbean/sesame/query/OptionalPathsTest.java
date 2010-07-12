/*
 * Copyright (c) 2010 Mysema Ltd.
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
import com.mysema.rdfbean.domains.NoteTermDomain;
import com.mysema.rdfbean.domains.NoteTermDomain.Note;
import com.mysema.rdfbean.domains.NoteTermDomain.Term;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.TestConfig;

/**
 * OptionalPathsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@TestConfig({Note.class, Term.class})
public class OptionalPathsTest extends SessionTestBase implements NoteTermDomain{
    
    private Session session;
    
    @Test
    public void test() throws StoreException, IOException{
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
