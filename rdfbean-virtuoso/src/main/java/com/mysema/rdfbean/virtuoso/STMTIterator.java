/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 * 
 */
public class STMTIterator implements CloseableIterator<STMT> {

    private final Converter converter;

    private final Statement stmt;

    private final ResultSet rs;

    @Nullable
    private final ID subject;

    @Nullable
    private final UID predicate;

    @Nullable
    private final NODE object;

    private final UID defaultGraph;

    @Nullable
    private Boolean next;

    private int graphColumn = -1, subjectColumn = -1, predicateColumn = -1,
            objectColumn = -1;

    public STMTIterator(
            Converter converter,
            Statement stmt,
            ResultSet rs,
            @Nullable ID subject,
            @Nullable UID predicate,
            @Nullable NODE object,
            UID defaultGraph) {
        this.converter = converter;
        this.stmt = stmt;
        this.rs = rs;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.defaultGraph = defaultGraph;
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String label = rsmd.getColumnName(i);
                if (label.equalsIgnoreCase("g")) {
                    graphColumn = i;
                } else if (label.equalsIgnoreCase("s")) {
                    subjectColumn = i;
                } else if (label.equalsIgnoreCase("p")) {
                    predicateColumn = i;
                } else if (label.equalsIgnoreCase("o")) {
                    objectColumn = i;
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void close() {
        AbstractQueryImpl.close(stmt, rs);
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            try {
                next = rs.next();
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }
        return next;
    }

    @Override
    public STMT next() {
        if (hasNext()) {
            next = null;
            try {
                return extractRow();
            } catch (SQLException e) {
                throw new RepositoryException(e);
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected STMT extractRow() throws SQLException {
        UID g = null;
        ID s = subject;
        UID p = predicate;
        NODE o = object;
        Object val = null;

        try {
            if (graphColumn != -1) {
                val = rs.getObject(graphColumn);
                g = (UID) converter.toNODE(val);
                if (defaultGraph.equals(g)) {
                    g = null;
                }
            }
        } catch (ClassCastException ccex) {
            throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting UID: " + val, ccex);
        }

        if (s == null) {
            try {
                val = rs.getObject(subjectColumn);
                s = (ID) converter.toNODE(val);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting ID: " + val, ccex);
            }
        }

        if (p == null) {
            try {
                val = rs.getObject(predicateColumn);
                p = (UID) converter.toNODE(val);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting UID: " + val, ccex);
            }
        }

        if (o == null) {
            o = converter.toNODE(rs.getObject(objectColumn));
        }

        return new STMT(s, p, o, g);
    }

}
