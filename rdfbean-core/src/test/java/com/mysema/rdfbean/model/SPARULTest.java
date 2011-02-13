package com.mysema.rdfbean.model;

import org.junit.Test;

public class SPARULTest {
    
    @Test
    public void Insert_into_Default(){
//        INSERT { <http://example/egbook3> dc:title  "This is an example title" }
    }
    
    @Test
    public void Insert_two_triples_into_Default(){
//        INSERT DATA
//        { <http://example/book3> dc:title    "A new book" ;
//                                 dc:creator  "A.N.Other" .
//        }
    }

    @Test
    public void Delete_from(){
//        DELETE DATA FROM <http://example/bookStore>
//        { <http://example/book3>  dc:title  "Fundamentals of Compiler Desing" }

    }

    @Test
    public void Insert_data_into(){
//        INSERT DATA INTO <http://example/bookStore>
//        { <http://example/book3>  dc:title  "Fundamentals of Compiler Design" }
    }
    
    @Test
    public void Delete_where(){
//        DELETE { ?book ?p ?v }
//       WHERE
//         { ?book dc:date ?date . FILTER ( ?date < "2000-01-01T00:00:00"^^xsd:dateTime )
//           ?book ?p ?v }
    }
    
}
