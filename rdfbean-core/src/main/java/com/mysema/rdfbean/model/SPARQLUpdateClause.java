package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.IteratorAdapter;

/**
 * @author tiwe
 * 
 */
public class SPARQLUpdateClause implements SPARQLUpdate {

    private static final Logger logger = LoggerFactory.getLogger(SPARQLUpdateClause.class);

    private final RDFConnection connection;

    private final UpdateClause clause;

    public SPARQLUpdateClause(RDFConnection connection, UpdateClause clause) {
        this.connection = connection;
        this.clause = clause;
    }

    public SPARQLUpdateClause(RDFConnection connection, String clause) {
        try {
            this.connection = connection;
            this.clause = new SPARQLUpdateParser().parse(clause);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void execute() {
        switch (clause.getType()) {
        case CLEAR:
        case DROP:
            connection.remove(null, null, null, clause.getSource());
        case CREATE:
        case LOAD:
            // TODO
            break;
        case COPY:
            // TODO
            break;
        case MOVE:
            // TODO
            break;
        case ADD:
            // TODO
            break;
        case DELETE:
            executeDelete();
            break;
        case INSERT:
            executeInsert();
            break;
        default:
            throw new IllegalStateException("Unknown clause " + clause.getType());
        }
    }

    private long executeInsert() {
        List<STMT> stmts = getTriples(clause.getTemplate(), clause.getPattern());
        connection.update(null, stmts);
        return 0l;
    }

    private long executeDelete() {
        List<STMT> stmts = getTriples(clause.getTemplate(), clause.getPattern());
        connection.update(stmts, null);
        return 0l;
    }

    private List<STMT> getTriples(String template, @Nullable String pattern) {
        // if (pattern == null ) {
        // // TODO : parse as Turtle
        // }

        StringBuilder qry = new StringBuilder();
        for (Map.Entry<String, String> prefix : clause.getPrefixes().entrySet()) {
            qry.append("PREFIX " + prefix.getKey() + ": <" + prefix.getValue() + ">\n");
        }
        qry.append("CONSTRUCT { " + template + " }\n");
        if (pattern != null) {
            for (UID uid : clause.getFrom()) {
                qry.append("FROM <" + uid.getId() + ">\n");
            }
            qry.append("WHERE { " + pattern + " }\n");
        } else {
            qry.append("WHERE { ?sss ?ppp ?ooo } LIMIT 1"); // XXX : improve
                                                            // this
        }
        logger.info(qry.toString());

        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, qry.toString());
        List<STMT> stmts = IteratorAdapter.asList(query.getTriples());

        if (clause.getInto().isEmpty() && clause.getFrom().isEmpty()) {
            return stmts;
        } else {
            List<UID> sources = clause.getInto().isEmpty() ? clause.getFrom() : clause.getInto();
            List<STMT> rv = new ArrayList<STMT>(stmts.size() * sources.size());
            for (STMT stmt : stmts) {
                for (UID uid : sources) {
                    rv.add(new STMT(stmt, uid));
                }
            }
            return rv;
        }

    }

}
