package com.mysema.rdfbean.jena;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.n3.N3TurtleJenaWriter;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.xmloutput.impl.Basic;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.io.Format;

/**
 * @author tiwe
 *
 */
public class GraphQueryImpl extends AbstractQueryImpl {
    
    public GraphQueryImpl(Query query, Dataset dataset, JenaDialect dialect) {
        super(query, dataset, dialect);
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
        QueryExecution exec = createExecution();
        Model resultModel = query.isConstructType() ? exec.execConstruct() : exec.execDescribe();
        ExtendedIterator<Triple> triples = resultModel.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
        return new TriplesIterator(dialect, triples);
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
    public void streamTriples(Writer w, String contentType) {
        Format format = Format.getFormat(contentType, Format.RDFXML);
        RDFWriter writer;
        if (format == Format.RDFXML){
            writer = new Basic();
        }else if (format == Format.NTRIPLES){
            writer = new NTripleWriter();            
        }else if (format == Format.TURTLE || format == Format.N3){
            writer = new N3TurtleJenaWriter();
        }else {
            throw new IllegalArgumentException(format.toString());
        }
        
        QueryExecution exec = createExecution();
        Model resultModel = query.isConstructType() ? exec.execConstruct() : exec.execDescribe();
        writer.write(resultModel, w, null);     
    }

}
