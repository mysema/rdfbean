package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.alias.Alias;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.rdfbean.domains.NoteRevisionTermDomain;
import com.mysema.rdfbean.domains.NoteRevisionTermDomain.Note;
import com.mysema.rdfbean.domains.NoteRevisionTermDomain.NoteRevision;
import com.mysema.rdfbean.domains.NoteRevisionTermDomain.Term;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.TestConfig;

@TestConfig({Note.class, NoteRevision.class, Term.class})
public class ComplexPathsTest extends AbstractRDBTest implements NoteRevisionTermDomain{
    
    private Session session;
    
    @Test
    public void optionalPaths() throws IOException{
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
    
    @Test
    public void deepPaths(){
        NoteRevision rev1 = new NoteRevision();
        NoteRevision rev2 = new NoteRevision();
        Note note = new Note();
        
        note.latestRevision = rev1;
        rev1.note = note;
        rev2.note = note;
        session.saveAll(note, rev1, rev2);

        NoteRevision noteVar = Alias.alias(NoteRevision.class);
        EBoolean where = $(noteVar).eq($(noteVar.getNote().getLatestRevision()));
        assertEquals(1, session.from($(noteVar)).where(where).list($(noteVar)).size());
    }
    
}