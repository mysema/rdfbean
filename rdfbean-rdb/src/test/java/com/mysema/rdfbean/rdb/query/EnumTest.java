package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static com.mysema.query.alias.Alias.alias;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.BooleanBuilder;
import com.mysema.rdfbean.domains.NoteTypeDomain;
import com.mysema.rdfbean.domains.NoteTypeDomain.Note;
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Note.class, NoteType.class})
public class EnumTest extends AbstractRDBTest implements NoteTypeDomain{
    
    private Note n = alias(Note.class, "n");
    
    @Before
    public void setUp(){
        Note note = new Note();
        note.type = NoteType.TYPE1;
        note.types = Collections.singleton(NoteType.TYPE1);
        session.save(note);   
        session.flush();
    }
    
    @Test
    public void test(){               
        assertEquals(0, session.from($(n)).where($(n.getType()).eq(NoteType.TYPE2)).list($(n)).size());
        assertEquals(1, session.from($(n)).where($(n.getType()).eq(NoteType.TYPE1)).list($(n)).size());
    }
    
    @Test
    public void test1(){        
        assertEquals(0, session.from($(n)).where($(n.getTypes()).contains(NoteType.TYPE2)).list($(n)).size());
        assertEquals(1, session.from($(n)).where($(n.getTypes()).contains(NoteType.TYPE1)).list($(n)).size());
    }
    
    @Test
    public void test2(){        
        BeanQuery query = session.from($(n));
        BooleanBuilder filter = new BooleanBuilder();
        filter.and($(n.getTypes()).contains(NoteType.TYPE1));
        filter.and($(n.getTypes()).contains(NoteType.TYPE2));
        assertEquals(0, query.where(filter).list($(n)).size());
    }
    
    @Test
    public void test3(){        
        BeanQuery query = session.from($(n));
        BooleanBuilder filter = new BooleanBuilder();
        filter.or($(n.getTypes()).contains(NoteType.TYPE1));
        filter.or($(n.getTypes()).contains(NoteType.TYPE2));
        assertEquals(1, query.where(filter).list($(n)).size());
    }
    
}
