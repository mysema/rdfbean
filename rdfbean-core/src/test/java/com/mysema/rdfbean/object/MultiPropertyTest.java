package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

@ClassMapping
public class MultiPropertyTest {

    private static final UID NAME = new UID(TEST.NS, "name");
    
    @Id(IDType.RESOURCE)
    ID id;

    @Predicate(ln = "name")
    Set<String> names = new HashSet<String>();

    @Test
    public void MultiProperties() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, MultiPropertyTest.class);
        
        MultiPropertyTest test = new MultiPropertyTest();
        test.names.add("Tom");
        test.names.add("Jane");
        session.save(test);
        
        List<STMT> stmts = IteratorAdapter.asList(repository.findStatements(null, NAME, null, null, false));
        assertEquals(2, stmts.size());
    }
    
}
