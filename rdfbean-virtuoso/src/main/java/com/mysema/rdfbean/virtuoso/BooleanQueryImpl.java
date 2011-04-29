package com.mysema.rdfbean.virtuoso;

import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class BooleanQueryImpl extends AbstractQueryImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(BooleanQueryImpl.class);
    
    public BooleanQueryImpl(Connection connection, int prefetch, String query) {
        super(connection, prefetch, query);
    }

    @Override
    public boolean getBoolean() {
        try {
            rs = executeQuery(query, false);
            rs.next();
            return rs.getShort(1) > 0;
        } catch (SQLException e) {
            logger.error(query);
            throw new RepositoryException(e);
        }finally{
            close();
        }
    }

    @Override
    public ResultType getResultType() {
        return ResultType.BOOLEAN;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();        
    }

}
