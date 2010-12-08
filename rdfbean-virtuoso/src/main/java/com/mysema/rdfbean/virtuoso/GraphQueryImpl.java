package com.mysema.rdfbean.virtuoso;

import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class GraphQueryImpl extends AbstractQueryImpl{
    
    private final Converter converter;
    
    private final SesameDialect dialect;
    
    public GraphQueryImpl(Connection connection, Converter converter, int prefetch, SesameDialect dialect, String query) {
        super(connection, prefetch, query);
        this.converter = converter;
        this.dialect = dialect;
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
        RDFFormat targetFormat = RDFFormat.forMIMEType(contentType);
        if (targetFormat == null){
            targetFormat = RDFFormat.RDFXML;
        }
        RDFWriter rdfWriter = Rio.createWriter(targetFormat, writer);
        try{
            rdfWriter.startRDF();
//            for (String prefix : model.getNamespaces().keySet()) {
//                rdfWriter.handleNamespace(prefix, model.getNamespace(prefix));
//            }
            CloseableIterator<STMT> stmts = getTriples();
            try{
                while (stmts.hasNext()){
                    STMT stmt = stmts.next();
                    Resource sub = dialect.getResource(stmt.getSubject());
                    URI pred = dialect.getURI(stmt.getPredicate());
                    Value obj = dialect.getNode(stmt.getObject());
                    rdfWriter.handleStatement(dialect.createStatement(sub, pred, obj));
                }    
            }finally{
                stmts.close();
            }
            rdfWriter.endRDF();   
        } catch (RDFHandlerException e) {
            close();
            throw new QueryException(e);
        }
    }

}
