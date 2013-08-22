/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.EntityPath;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.domains.EntityDomain.Entity;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(Entity.class)
public class BeanSubQueryTest extends SessionTestBase implements EntityDomain {

    private final List<DateTime> dateTimes = new ArrayList<DateTime>();

    private final Entity var1 = Alias.alias(Entity.class, "var1");

    private final Entity var2 = Alias.alias(Entity.class, "var2");

    @Before
    public void setUp() {
        DateTime dateTime = new DateTime();
        dateTime = dateTime.minus(dateTime.getMillisOfSecond());
        for (Long rev : Arrays.asList(5l, 10l, 15l, 20l, 25l, 30l)) {
            Entity entity = new Entity(rev, "text" + rev, dateTime.plusMinutes(rev.intValue()));
            dateTimes.add(entity.getCreated());
            session.save(entity);
        }
        session.clear();
    }

    @Test
    public void CompareLong() throws IOException {
        Entity result = session.from($(var1))
                .where(
                        sub($(var2))
                                .where($(var2).ne($(var1)), $(var2.getRevision()).gt($(var1.getRevision())))
                                .notExists())
                .uniqueResult($(var1));
        assertNotNull(result);
        assertEquals(30l, result.getRevision());
    }

    @Test
    public void CompareDateTime() {
        Entity result = session.from($(var1))
                .where(
                        sub($(var2))
                                .where($(var2).ne($(var1)), $(var2.getCreated()).gt($(var1.getCreated())))
                                .notExists())
                .uniqueResult($(var1));
        assertNotNull(result);
        assertEquals(dateTimes.get(dateTimes.size() - 1).getMillis(), result.getCreated().getMillis());
    }

    @Test
    public void PathVisibility() {
        int count = session.from($(var1))
                .where(
                        sub($(var2))
                                .where(
                                        $(var1.getText()).ne($(var2.getText())))
                                .exists())
                .list($(var1.getText())).size();
        assertEquals(6, count);
    }

    private BeanSubQuery sub(EntityPath<?> entity) {
        return new BeanSubQuery().from(entity);
    }

}
