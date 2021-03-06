/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

public class ParsingTest {

    @Test
    public void TupleQuery() throws MalformedQueryException, UnsupportedQueryLanguageException {
        List<String> queries = new ArrayList<String>();
        queries.add("SELECT R FROM {R} rdfs:label {L}");
        queries.add("SELECT L, R FROM {R} rdfs:label {L}");
        queries.add("SELECT R FROM {R} rdfs:label {\"Hello World\"}");
        queries.add("SELECT R FROM {R} rdfs:label {\"Hello World\"^^xsd:string}");
        queries.add("SELECT label(L), lang(L) FROM {R} rdfs:label {L}");
        queries.add("SELECT xsd:integer(L) FROM {R} rdfs:label {L}");
        queries.add("SELECT R FROM {R} rdfs:label {L} LIMIT 1");
        queries.add("SELECT R FROM {R} rdfs:label {L} OFFSET 2");
        queries.add("SELECT R FROM {R} rdfs:label {L} LIMIT 2 OFFSET 3");

        for (String query : queries) {
            ParsedTupleQuery model = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query, null);
            System.out.println(model);
            System.out.println();
        }
    }

    @Test
    public void GraphQuery() throws MalformedQueryException, UnsupportedQueryLanguageException {
        List<String> queries = new ArrayList<String>();
        queries.add("CONSTRUCT {S} rdf:type {O} FROM {S} rdf:type {O}");

        for (String query : queries) {
            ParsedGraphQuery model = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL, query, null);
            System.out.println(model);
            System.out.println();
        }
    }

}
