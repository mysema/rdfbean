package com.mysema.rdfbean.jena;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.hp.hpl.jena.n3.N3TurtleJenaWriter;
import com.hp.hpl.jena.n3.turtle.TurtleReader;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.xmloutput.impl.Basic;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;

/**
 * @author tiwe
 *
 */
public class JenaRepository implements Repository{

    protected final DatasetGraph graph;
    
    protected final Dataset dataset;
    
    private final JenaDialect dialect = new JenaDialect();
    
    private RDFSource[] sources;
    
    private boolean initialized = false;
    
    public JenaRepository(DatasetGraph graph, Dataset dataset) {
        this.graph = graph;
        this.dataset = dataset;
    }
    
    public JenaRepository(Dataset dataset) {
        this.graph = dataset.asDatasetGraph();
        this.dataset = dataset;
    }
    
    @Override
    public void close() {
        graph.close();        
    }

    @Override
    public <RT> RT execute(Operation<RT> operation) {        
        RDFConnection connection = openConnection();
        try{
//            if (graph.getTransactionHandler().transactionsSupported()){
//                RDFBeanTransaction tx = connection.beginTransaction(false, 
//                        RDFBeanTransaction.TIMEOUT, 
//                        RDFBeanTransaction.ISOLATION);
//                try{
//                    RT retVal = operation.execute(connection);
//                    tx.commit();
//                    return retVal;
//                }catch(IOException io){
//                    tx.rollback();
//                    throw new RepositoryException(io);
//                }    
//            }else{
                try{
                    return operation.execute(connection);
                }catch(IOException io){
                    throw new RepositoryException(io);
                }
//            }            
        }finally{
            connection.close();
        }
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, OutputStream os) {
        RDFWriter writer;
        if (format == Format.RDFXML){
            Basic w = new Basic();
            for (Map.Entry<String, String> entry : ns2prefix.entrySet()){
                w.setNsPrefix(entry.getValue(), entry.getKey());
            }
            writer = w;                  
        }else if (format == Format.NTRIPLES){
            writer = new NTripleWriter();            
        }else if (format == Format.TURTLE || format == Format.N3){
            writer = new N3TurtleJenaWriter();
        }else {
            throw new IllegalArgumentException(format.toString());
        }
        
        // TODO : export also other models
        writer.write(dataset.getDefaultModel(), os, null);        
    }

    @Override
    public void export(Format format, OutputStream os) {
        export(format, Namespaces.DEFAULT, os);        
    }

    @Override
    public void initialize() {
        if (!initialized){
            if (sources != null){
                try{
                    for (RDFSource source : sources){
                        load(source.getFormat(), source.openStream(),  new UID(source.getContext()), false);
                    }
                } catch(IOException e){
                    throw new RepositoryException(e);
                }
            }
            initialized = true;
        }
    }

    @Override
    public void load(Format format, InputStream is, UID context, boolean replace) {
        RDFReader reader;
        if (format == Format.RDFXML){
            reader = new JenaReader();
        }else if (format == Format.TURTLE || format == Format.NTRIPLES){
            reader = new TurtleReader();
        }else {
            throw new IllegalArgumentException(format.toString());
        }   
        Model model = context == null ? dataset.getDefaultModel() : dataset.getNamedModel(context.getId());
        Assert.notNull(model, "model");
        reader.read(model, is, context != null ? context.getId() : null);
    }

    @Override
    public RDFConnection openConnection() {
        return new JenaConnection(graph, dataset, dialect);
    }
    
    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }

}
