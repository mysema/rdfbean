package com.mysema.rdfbean.jena;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.n3.N3TurtleJenaWriter;
import com.hp.hpl.jena.n3.turtle.TurtleReader;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFBeanTransaction;
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

    private final Graph graph;
    
    private final Model model;
    
    private final JenaDialect dialect = new JenaDialect();
    
    private RDFSource[] sources;
    
    private boolean initialized = false;
    
    public JenaRepository(Model model) {
        this.graph = model.getGraph();
        this.model = model;
    }
    
    public JenaRepository(Graph graph) {
        this.graph = graph;
        this.model = ModelFactory.createModelForGraph(graph);
    }
    
    @Override
    public void close() {
        graph.close();        
    }

    @Override
    public <RT> RT execute(Operation<RT> operation) {        
        RDFConnection connection = openConnection();
        try{
            if (graph.getTransactionHandler().transactionsSupported()){
                RDFBeanTransaction tx = connection.beginTransaction(false, 
                        RDFBeanTransaction.TIMEOUT, 
                        RDFBeanTransaction.ISOLATION);
                try{
                    RT retVal = operation.execute(connection);
                    tx.commit();
                    return retVal;
                }catch(IOException io){
                    tx.rollback();
                    throw new RepositoryException(io);
                }    
            }else{
                try{
                    return operation.execute(connection);
                }catch(IOException io){
                    throw new RepositoryException(io);
                }
            }            
        }finally{
            connection.close();
        }
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, OutputStream os) {
        RDFWriter writer;
        if (format == Format.RDFXML){
            writer = new NTripleWriter();
        }else if (format == Format.TURTLE || format == Format.NTRIPLES){
            writer = new N3TurtleJenaWriter();
        }else {
            throw new IllegalArgumentException(format.toString());
        }
        
        writer.write(model, os, null);        
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
        reader.read(model, is, context != null ? context.getId() : null);
    }

    @Override
    public RDFConnection openConnection() {
        return new JenaConnection(graph, dialect);
    }
    
    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }

}
