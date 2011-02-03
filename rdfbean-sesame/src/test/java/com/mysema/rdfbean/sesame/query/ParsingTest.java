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
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.TupleQueryModel;



/**
 * ParsingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ParsingTest {

    @Test
    public void TupleQuery() throws MalformedQueryException, UnsupportedQueryLanguageException{
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
        queries.add("SELECT val FROM {node} <ex:value> {val} WHERE val >= ANY ( SELECT value FROM {} <ex:value> {value} )");

        for (String query : queries){
            TupleQueryModel model = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query, null);
            System.out.println(model);
            System.out.println();
        }
    }

    @Test
    public void GraphQuery(){
        List<String> queries = new ArrayList<String>();
        queries.add("CONSTRUCT {S} rdf:type {O} FROM {S} rdf:type {O}");

        for (String query : queries){
            GraphQueryModel model = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL, query, null);
            System.out.println(model);
            System.out.println();
        }
    }

    @Test
    public void GraphQuery2(){
        List<String> queries = new ArrayList<String>();
        queries.add("CONSTRUCT { ?s ?p ?o ; <test:test> ?o .} WHERE { ?s ?p ?o }");

        for (String query : queries){
            GraphQueryModel model = QueryParserUtil.parseGraphQuery(QueryLanguage.SPARQL, query, null);
            System.out.println(model);
            System.out.println();
        }
    }

}
