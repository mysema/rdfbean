package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
import com.mysema.rdfbean.xsd.DateTimeConverter;
import com.mysema.rdfbean.xsd.LocalDateConverter;
import com.mysema.rdfbean.xsd.LocalTimeConverter;

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
        
        // load data
        ID sub = new BID();
        UID pre = new UID(TEST.NS, "test");
        repository.execute(new Addition(
                new STMT(sub, pre, new LIT(dateTime.toString(new DateTime()), XSD.dateTime)),
                new STMT(sub, pre, new LIT(localDate.toString(new LocalDate()), XSD.date)),
                new STMT(sub, pre, new LIT(localTime.toString(new LocalTime()), XSD.time))
        ));
        long count = repository.execute(new CountOperation());
        assertEquals(3, count);
        
        // export
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, out);
        System.out.println(new String(out.toByteArray()));
        
        // import
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        repository.load(Format.TURTLE, in, new UID(TEST.NS), true);
        
        count = repository.execute(new CountOperation());
        assertEquals(6, count);
    }

}
