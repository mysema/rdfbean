package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.query.QueryFunctions;

public class UtilDateConverterTest {
    
    private Locale defaultLocale;
    
    @Before
    public void setUp(){
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }
    
    @After
    public void tearDown(){
        Locale.setDefault(defaultLocale);
    }
    
    private UtilDateConverter converter = new UtilDateConverter();
    
    @Test
    public void test(){
        Date date = new Date();
        String str = converter.toString(date);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(
            String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)), 
            QueryFunctions.week(str));
    }
    
    @Test
    public void parse1(){
        // Thu Jul 14 05:18:56 EEST 2005 failed
        Date date = converter.fromString("2005-07-14T05:18:56+03:00");
        System.out.println(date);
        System.out.println(converter.toString(date));
        Date date2 = converter.fromString(converter.toString(date));
        assertEquals(date, date2);
    }
    
    @Test
    public void parse2(){        
        Date date = converter.fromString("2006-07-14T02:18:56+03:00");
        Date date2 = converter.fromString(converter.toString(date));
        assertEquals(date, date2);
    }

}

