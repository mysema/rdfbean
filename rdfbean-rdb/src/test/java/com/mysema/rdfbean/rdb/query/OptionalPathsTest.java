package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.NoteTermDomain;
import com.mysema.rdfbean.domains.NoteTermDomain.Note;
import com.mysema.rdfbean.domains.NoteTermDomain.Term;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Note.class, Term.class})
public class OptionalPathsTest extends AbstractRDBTest implements NoteTermDomain{
    
    @Test
    public void test() throws IOException{
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
