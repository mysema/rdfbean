package com.mysema.rdfbean.virtuoso;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.sesame.SesameDialect;

public class RDFStreamingHandler implements RDFHandler {

    private final RDFConnection connection;
    
    private final UID currentContext;

    private final SesameDialect dialect;
    
    private final List<STMT> statements;
    
    private int currentCount;

    public RDFStreamingHandler(RDFConnection connection, UID context) {
        this.connection = connection;
        this.currentContext = context;
        this.dialect = new SesameDialect(ValueFactoryImpl.getInstance());
        this.statements = new ArrayList<STMT>();
        this.currentCount = 0;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        // nothing to do
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        flushAndClearStatements();
    }

    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
        // nothing to do
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        final ID subject = dialect.getID(st.getSubject());
        final UID predicate = dialect.getUID(st.getPredicate());
        final NODE object = dialect.getNODE(st.getObject());
        statements.add(new STMT(subject, predicate, object, currentContext));
        currentCount++;
        if (currentCount > 5000) {
            flushAndClearStatements();
        }
    }

    private void flushAndClearStatements() {
        connection.update(null, statements);
        statements.clear();
        currentCount = 0;
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // nothing to do
    }

}
