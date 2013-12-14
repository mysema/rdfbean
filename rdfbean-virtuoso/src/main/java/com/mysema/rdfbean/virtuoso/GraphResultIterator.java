/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 * 
 */
public class GraphResultIterator implements CloseableIterator<STMT> {

    private static final Logger logger = LoggerFactory.getLogger(GraphResultIterator.class);

    private final Statement stmt;

    private final ResultSet rs;

    private final String query;

    private final Converter converter;

    @Nullable
    private Boolean next = null;

    private final boolean hasContext;

    public GraphResultIterator(Statement stmt, ResultSet rs, String query, Converter converter) {
        try {
            this.stmt = stmt;
            this.rs = rs;
            this.hasContext = rs.getMetaData().getColumnCount() > 3;
            this.query = query;
            this.converter = converter;
        } catch (SQLException e) {
            throw new QueryException(e);
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
                logger.warn("Caught exception for query " + query, e);
                throw new QueryException(e);
            }
        }
        return next;
    }

    @Override
    public STMT next() {
        if (hasNext()) {
            try {
                next = null;
                ID subject = (ID) converter.toNODE(rs.getObject(1));
                UID predicate = (UID) converter.toNODE(rs.getObject(2));
                NODE object = converter.toNODE(rs.getObject(3));
                if (hasContext) {
                    UID context = (UID) converter.toNODE(rs.getObject(4));
                    return new STMT(subject, predicate, object, context);
                } else {
                    return new STMT(subject, predicate, object);
                }

            } catch (SQLException e) {
                close();
                logger.warn("Caught exception for query " + query, e);
                throw new QueryException(e);
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
