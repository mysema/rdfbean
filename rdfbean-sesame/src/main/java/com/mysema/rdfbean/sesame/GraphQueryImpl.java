package com.mysema.rdfbean.sesame;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.util.ModelOrganizer;
import org.openrdf.query.GraphQuery;
import org.openrdf.result.GraphResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.store.StoreException;

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
        } catch (StoreException e) {
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
            GraphResult result = graphQuery.evaluate();
            Model model = result.asModel();
            model = new ModelOrganizer(model).organize();
            RDFFormat targetFormat = RDFFormat.forMIMEType(contentType);
            if (targetFormat == null) {
                targetFormat = RDFFormat.RDFXML;
            }
            RDFWriter rdfWriter = Rio.createWriter(targetFormat, writer);
            rdfWriter.startRDF();
            for (String prefix : model.getNamespaces().keySet()) {
                rdfWriter.handleNamespace(prefix, model.getNamespace(prefix));
            }
            for (Statement st : model) {
                rdfWriter.handleStatement(st);
            }
            rdfWriter.endRDF();

        } catch (StoreException e) {
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
