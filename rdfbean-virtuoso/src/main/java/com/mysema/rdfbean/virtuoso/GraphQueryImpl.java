/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso;

import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.STMTComparator;
import com.mysema.rdfbean.model.io.RDFWriter;
import com.mysema.rdfbean.model.io.WriterUtils;

/**
 * @author tiwe
 * 
 */
public class GraphQueryImpl extends AbstractQueryImpl {

    private static final Logger logger = LoggerFactory.getLogger(GraphQueryImpl.class);

    private final Converter converter;

    public GraphQueryImpl(Connection connection, Converter converter, int prefetch, String query) {
        super(connection, prefetch, query);
        this.converter = converter;
    }

    @Override
    public boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultType getResultType() {
        return ResultType.TRIPLES;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        try {
            rs = executeQuery(query, false);
            List<STMT> stmts = IteratorAdapter.asList(new GraphResultIterator(stmt, rs, query, converter));
            Collections.sort(stmts, STMTComparator.DEFAULT);
            return new IteratorAdapter<STMT>(stmts.iterator());
        } catch (SQLException e) {
            logger.error(query);
            close();
            throw new RepositoryException(e);
        }
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        Format format = Format.getFormat(contentType, Format.RDFXML);
        RDFWriter rdfWriter = WriterUtils.createWriter(format, writer);
        CloseableIterator<STMT> stmts = getTriples();
        try {
            rdfWriter.begin();
            while (stmts.hasNext()) {
                rdfWriter.handle(stmts.next());
            }
            rdfWriter.end();
        } finally {
            stmts.close();
        }
    }

}
