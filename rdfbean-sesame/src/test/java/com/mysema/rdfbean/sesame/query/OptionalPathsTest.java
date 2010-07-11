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
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * OptionalPathsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OptionalPathsTest extends SessionTestBase implements NoteTermDomain{
    
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
