package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Addition;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.CountOperation;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.xsd.DateConverter;
import com.mysema.rdfbean.xsd.DateTimeConverter;
import com.mysema.rdfbean.xsd.LocalDateConverter;
import com.mysema.rdfbean.xsd.LocalTimeConverter;
import com.mysema.rdfbean.xsd.TimeConverter;
import com.mysema.rdfbean.xsd.TimestampConverter;
import com.mysema.rdfbean.xsd.UtilDateConverter;

public class DateTimePersistenceTest {
    
    private MemoryRepository repository;
    
    @After
    public void tearDown(){
        if (repository != null){
            repository.close();
        }
    }
    
    @Test
    public void Round_Trip(){
        repository = new MemoryRepository();
        repository.initialize();
        
        DateTimeConverter dateTime = new DateTimeConverter();
        LocalDateConverter localDate = new LocalDateConverter();
        LocalTimeConverter localTime = new LocalTimeConverter();
        DateConverter date = new DateConverter();
        UtilDateConverter utilDate = new UtilDateConverter();
        TimeConverter time = new TimeConverter();
        TimestampConverter timestamp = new TimestampConverter();
        
        // load data
        ID sub = new BID();
        repository.execute(new Addition(
                new STMT(sub, pre(1), new LIT(dateTime.toString(new DateTime()), XSD.dateTime)),
                new STMT(sub, pre(2), new LIT(localDate.toString(new LocalDate()), XSD.date)),
                new STMT(sub, pre(3), new LIT(localTime.toString(new LocalTime()), XSD.time)),
                
                new STMT(sub, pre(4), new LIT(date.toString(new java.sql.Date(0)), XSD.date)),
                new STMT(sub, pre(5), new LIT(utilDate.toString(new java.util.Date(0)), XSD.dateTime)),
                new STMT(sub, pre(6), new LIT(time.toString(new Time(0)), XSD.time)),
                new STMT(sub, pre(7), new LIT(timestamp.toString(new Timestamp(0)), XSD.dateTime))
        ));
        long count = repository.execute(new CountOperation());
        assertEquals(7, count);
        
        // export
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, out);
        System.out.println(new String(out.toByteArray()));
        
        // import
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        repository.load(Format.TURTLE, in, new UID(TEST.NS), true);
        
        count = repository.execute(new CountOperation());
        assertEquals(7*2, count);
    }

    private UID pre(int i){
        return new UID(TEST.NS, "test"+i);
    }
    
}
