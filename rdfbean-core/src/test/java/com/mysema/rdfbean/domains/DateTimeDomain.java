/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface DateTimeDomain {
    
    @ClassMapping(ns=TEST.NS)
    public class Literals{
     
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public java.util.Date date;
        
        @Predicate
        public java.sql.Date date2;
        
        @Predicate
        public java.sql.Time time;
        
        @Predicate
        public java.sql.Timestamp timestamp;
        
        @Predicate
        public LocalDate localDate;
        
        @Predicate
        public DateTime dateTime;
        
        @Predicate
        public LocalTime localTime;

        public ID getId() {
            return id;
        }

        public java.util.Date getDate() {
            return date;
        }

        public java.sql.Date getDate2() {
            return date2;
        }

        public java.sql.Time getTime() {
            return time;
        }

        public java.sql.Timestamp getTimestamp() {
            return timestamp;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public DateTime getDateTime() {
            return dateTime;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }
                
    }

}
