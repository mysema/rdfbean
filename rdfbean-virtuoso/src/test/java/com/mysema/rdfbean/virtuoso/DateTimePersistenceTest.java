package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Addition;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

public class DateTimePersistenceTest extends AbstractConnectionTest {

    @Test
    public void Round_Trip(){
        ConverterRegistry converters = new ConverterRegistryImpl();

        // load data
        ID sub = new BID();
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, pre(1), new LIT(converters.toString(new DateTime(2000,1,1,1,1,1,0)), XSD.dateTime)),
                new STMT(sub, pre(2), new LIT(converters.toString(new LocalDate(2000,1,1)), XSD.date)),
                new STMT(sub, pre(3), new LIT(converters.toString(new LocalTime(5,2,3)), XSD.time)),

                new STMT(sub, pre(4), new LIT(converters.toString(new java.sql.Date(0)), XSD.date)),
                new STMT(sub, pre(5), new LIT(converters.toString(new java.util.Date(0)), XSD.dateTime)),
                new STMT(sub, pre(6), new LIT(converters.toString(new Time(0)), XSD.time)),
                new STMT(sub, pre(7), new LIT(converters.toString(new Timestamp(0)), XSD.dateTime)));
        toBeRemoved = stmts;
        repository.execute(new Addition(stmts.toArray(new STMT[stmts.size()])));

        List<STMT> queried = IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false));
        assertEquals(stmts.size(), queried.size());
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(queried));
    }

    private UID pre(int i){
        return new UID(TEST.NS, "test"+i);
    }

}
