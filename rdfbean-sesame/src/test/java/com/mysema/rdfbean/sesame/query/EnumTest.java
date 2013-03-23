package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static com.mysema.query.alias.Alias.alias;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.BooleanBuilder;
import com.mysema.rdfbean.domains.NoteTypeDomain;
import com.mysema.rdfbean.domains.NoteTypeDomain.Note;
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Note.class, NoteType.class })
public class EnumTest extends SessionTestBase implements NoteTypeDomain {

    private Note n = alias(Note.class, "n");

    @Before
    public void setUp() {
        // note with types
        Note note = new Note();
        note.type = NoteType.TYPE1;
        note.types = Collections.singleton(NoteType.TYPE1);
        session.save(note);

        // note without types
        session.save(new Note());
        session.flush();
    }

    @Test
    public void Order() {
        session.save(new Note(NoteType.A));
        session.save(new Note(NoteType.B));
        session.flush();

        assertEquals(
                Arrays.asList(null, NoteType.A, NoteType.B, NoteType.TYPE1),
                session.from($(n)).orderBy($(n.getType()).asc()).list($(n.getType())));
    }

    @Test
    public void Order_by_ordinal() throws IOException {
        session.save(new Note(NoteType.A));
        session.save(new Note(NoteType.B));
        session.flush();
        assertEquals(
                Arrays.asList(null, NoteType.TYPE1, NoteType.A, NoteType.B),
                session.from($(n)).orderBy($(n.getType()).ordinal().asc()).list($(n.getType())));
    }

    @Test
    public void test() {
        assertEquals(0, session.from($(n)).where($(n.getType()).eq(NoteType.TYPE2)).list($(n)).size());
        assertEquals(1, session.from($(n)).where($(n.getType()).eq(NoteType.TYPE1)).list($(n)).size());
    }

    @Test
    public void test1() {
        assertEquals(0, session.from($(n)).where($(n.getTypes()).contains(NoteType.TYPE2)).list($(n)).size());
        assertEquals(1, session.from($(n)).where($(n.getTypes()).contains(NoteType.TYPE1)).list($(n)).size());
    }

    @Test
    public void test2() {
        BeanQuery query = session.from($(n));
        BooleanBuilder filter = new BooleanBuilder();
        filter.and($(n.getTypes()).contains(NoteType.TYPE1));
        filter.and($(n.getTypes()).contains(NoteType.TYPE2));
        assertEquals(0, query.where(filter).list($(n)).size());
    }

    @Test
    public void test3() {
        BeanQuery query = session.from($(n));
        BooleanBuilder filter = new BooleanBuilder();
        filter.or($(n.getTypes()).contains(NoteType.TYPE1));
        filter.or($(n.getTypes()).contains(NoteType.TYPE2));
        assertEquals(1, query.where(filter).list($(n)).size());
    }

    @Test
    public void test4() {
        BeanQuery query = session.from($(n));
        BooleanBuilder filter = new BooleanBuilder();
        filter.or($(n.getType()).eq(NoteType.TYPE1));
        filter.or($(n.getType()).eq(NoteType.TYPE2));
        assertEquals(1, query.where(filter).list($(n)).size());
    }
}
