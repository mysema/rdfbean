package com.mysema.rdfbean.sesame;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 * 
 */
public class GraphQueryImpl implements SPARQLQuery {

    private final SesameDialect dialect;

    private final GraphQuery graphQuery;

    public GraphQueryImpl(GraphQuery graphQuery, SesameDialect dialect) {
        this.graphQuery = graphQuery;
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
            return new GraphResultIterator(graphQuery.evaluate(), dialect);
        } catch (QueryEvaluationException e) {
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
        try {
            RDFFormat targetFormat = RDFFormat.forMIMEType(contentType);
            if (targetFormat == null) {
                targetFormat = RDFFormat.RDFXML;
            }
            RDFWriter rdfWriter = Rio.createWriter(targetFormat, writer);
            graphQuery.evaluate(rdfWriter);
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void setBinding(String variable, NODE node) {
        graphQuery.setBinding(variable, dialect.getNode(node));
    }

    @Override
    public void setMaxQueryTime(int secs) {
        graphQuery.setMaxQueryTime(secs);
    }

}
