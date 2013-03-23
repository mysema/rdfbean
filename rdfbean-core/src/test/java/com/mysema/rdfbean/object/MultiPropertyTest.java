package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;
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

    private static final UID NAME2 = new UID(TEST.NS, "name2");

    private static final UID NAME3 = new UID(TEST.NS, "name3");

    @Id(IDType.RESOURCE)
    ID id;

    @Predicate(ln = "name")
    Set<String> names = new HashSet<String>();

    @Predicate(ln = "name2")
    @Container(ContainerType.NONE)
    List<String> namesList = new ArrayList<String>();

    @Predicate(ln = "name3")
    @Container(ContainerType.NONE)
    String[] namesArray;

    @Test
    public void MultiProperties() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, MultiPropertyTest.class);

        MultiPropertyTest test = new MultiPropertyTest();
        test.names.add("Tom");
        test.names.add("Jane");
        test.namesList.add("Tom");
        test.namesList.add("Jane");
        test.namesArray = new String[] { "Tom", "Jane" };
        session.save(test);
        session.clear();

        List<STMT> stmts = IteratorAdapter.asList(repository.findStatements(null, NAME, null, null, false));
        assertEquals(2, stmts.size());
        stmts = IteratorAdapter.asList(repository.findStatements(null, NAME2, null, null, false));
        assertEquals(2, stmts.size());
        stmts = IteratorAdapter.asList(repository.findStatements(null, NAME3, null, null, false));
        assertEquals(2, stmts.size());

        MultiPropertyTest test2 = session.get(MultiPropertyTest.class, test.id);
        assertEquals(test.names, test2.names);
        // ordering is not preseved
        assertEquals(Sets.newHashSet(test.namesList), Sets.newHashSet(test2.namesList));
        // ordering is not preseved
        assertEquals(Sets.newHashSet(test.namesArray), Sets.newHashSet(test2.namesArray));
    }

}
