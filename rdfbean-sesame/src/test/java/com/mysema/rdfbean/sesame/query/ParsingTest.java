/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.parser.QueryParserUtil;

/**
 * ParsingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ParsingTest {

    @Test
    public void parse() throws MalformedQueryException, UnsupportedQueryLanguageException{
        print("SELECT R FROM {R} rdfs:label {L}");        
        print("SELECT L, R FROM {R} rdfs:label {L}");
        print("SELECT R FROM {R} rdfs:label {\"Hello World\"}");
        print("SELECT R FROM {R} rdfs:label {\"Hello World\"^^xsd:string}");
    }
    
    private void print(String query) throws MalformedQueryException, UnsupportedQueryLanguageException {
        System.out.println(QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query, null));
        
    }
}
