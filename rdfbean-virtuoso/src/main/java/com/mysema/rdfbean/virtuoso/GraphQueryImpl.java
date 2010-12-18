package com.mysema.rdfbean.virtuoso;

import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFWriter;
import com.mysema.rdfbean.model.io.WriterUtils;

/**
 * @author tiwe
 *
 */
public class GraphQueryImpl extends AbstractQueryImpl{
    
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
            rs = executeQuery(query);
            return new GraphResultIterator(stmt, rs, query, converter);
        } catch (SQLException e) {
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
        try{
            rdfWriter.begin();
            while (stmts.hasNext()){
                rdfWriter.handle(stmts.next());
            }    
            rdfWriter.end();
        }finally{
            stmts.close();
        }
    }

}
