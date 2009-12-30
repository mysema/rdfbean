package com.mysema.rdfbean.sesame;

import java.util.Set;
import java.util.TreeSet;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import com.mysema.commons.lang.Assert;

/**
 * ExtendedTurtleWriter provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class RDFWriterAdapter implements RDFWriter {

    private final Set<Statement> statements = new TreeSet<Statement>(new StatementComparator());
    
    private final RDFWriter writer;

    public RDFWriterAdapter(RDFWriter writer) {
        this.writer = Assert.notNull(writer);
    }
    
    @Override
    public void handleStatement(Statement stmt) throws RDFHandlerException{
        statements.add(stmt);
    }

    @Override
    public RDFFormat getRDFFormat() {
        return writer.getRDFFormat();
    }

    @Override
    public void setBaseURI(String arg0) {
        writer.setBaseURI(arg0);        
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        for (Statement stmt : statements){
            writer.handleStatement(stmt);
        }        
        statements.clear();
        writer.endRDF();                
    }

    @Override
    public void handleComment(String arg0) throws RDFHandlerException {
        writer.handleComment(arg0);        
    }

    @Override
    public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
        writer.handleNamespace(arg0, arg1);        
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        writer.startRDF();        
    }

}
