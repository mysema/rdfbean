package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.IteratorAdapter;

public class RDFUpdate {
    
    private final RDFConnection connection;
    
    private final UpdateClause clause;
    
    public RDFUpdate(RDFConnection connection, String clause) {        
        try {
            this.connection = connection;
            this.clause = new SPARQLUpdateParser().parse(clause);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    
    public long execute(){
        switch(clause.getType()){
        case CLEAR: 
        case DROP:
            connection.remove(null, null, null, clause.getSource());
            return 0l;
        case CREATE: 
            return 0l;
        case LOAD: 
            // TODO
            return 0l;
        case DELETE: return executeDelete();
        case INSERT: return executeInsert();
        case MODIFY: return executeModify();
        default: throw new IllegalStateException("Unknown clause " + clause.getType());
        }
    }

    private long executeModify() {
        // TODO Auto-generated method stub
        return 0;
    }

    private long executeInsert() {
        List<STMT> stmts = getTriples();
        connection.update(null, stmts);
        return 0l;
    }

    private long executeDelete() {
        List<STMT> stmts = getTriples();
        connection.update(stmts, null);
        return 0l;
    }
    
    private List<STMT> getTriples(){
        List<UID> from = clause.getFrom();
        String pattern = clause.getPattern();
        String template = clause.getTemplate();
        StringBuilder qry = new StringBuilder();
        for (Map.Entry<String, String> prefix : clause.getPrefixes().entrySet()){
            qry.append("PREFIX " + prefix.getKey() + ": <" + prefix.getValue() + ">\n");
        }
        qry.append("CONSTRUCT { " + template +" }\n");
        for (UID uid : from){
            qry.append("FROM <" + uid.getId() + ">\n");
        }
        if (pattern != null){
            qry.append("WHERE { " + pattern + " }\n");
        }else{
            qry.append("WHERE { ?sss ?ppp ?ooo } LIMIT 1"); // XXX : improve this
        }
//        System.err.println(qry);
        
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, qry.toString());
        return IteratorAdapter.asList(query.getTriples());
    }
    
}
