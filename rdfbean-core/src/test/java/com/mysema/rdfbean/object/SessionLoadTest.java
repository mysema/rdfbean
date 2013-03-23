package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.domains.NoteTermDomain.Note;
import com.mysema.rdfbean.domains.NoteTermDomain.Term;
import com.mysema.rdfbean.model.ID;

public class SessionLoadTest {

    private Session session;

    private ID termId, note1Id, note2Id;

    @Before
    public void setUp() {
        session = SessionUtil.openSession(Note.class, Term.class);
        Term term = new Term();
        term.meaning = "X";

        Term term2 = new Term();
        term2.meaning = "X";

        Note note1 = new Note();
        note1.basicForm = "A";
        note1.lemma = "B";
        note1.term = term;

        Note note2 = new Note();
        note2.basicForm = "A";
        note2.lemma = "B";
        note2.term = term2;

        session.saveAll(term, term2, note1, note2);

        termId = term.id;
        note1Id = note1.id;
        note2Id = note2.id;
        session.clear();
        System.out.println();
    }

    @Test
    public void FindInstances() {
        System.out.println("Get all notes");
        List<Note> notes = session.findInstances(Note.class);
        assertEquals(2, notes.size());
    }

    @Test
    public void QueryAll() {
        System.out.println("Query all notes");
        PathBuilder<Note> note = new PathBuilder<Note>(Note.class, "note");
        List<Note> notes = session.from(note).list(note);
        assertEquals(2, notes.size());
    }

    @Test
    public void GetAll() {
        System.out.println("Get both notes");
        List<Note> notes = session.getAll(Note.class, note1Id, note2Id);
        assertEquals(2, notes.size());
    }

    @Test
    public void GetNote() {
        System.out.println("Get note1");
        Note note = session.get(Note.class, note1Id);
        assertNotNull(note);
    }

    @Test
    public void GetTerm() {
        System.out.println("Get term");
        Term term = session.get(Term.class, termId);
        assertNotNull(term);
    }

}
