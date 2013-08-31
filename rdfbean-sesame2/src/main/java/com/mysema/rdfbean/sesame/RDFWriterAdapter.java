/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

import com.mysema.commons.lang.Assert;

/**
 * @author tiwe
 */
public class RDFWriterAdapter extends RDFHandlerWrapper implements RDFWriter {

    private final Set<Statement> statements = new TreeSet<Statement>(new StatementComparator());

    private final RDFWriter writer;

    public RDFWriterAdapter(RDFWriter writer) {
        super(writer);
        this.writer = Assert.notNull(writer, "writer");
    }

    @Override
    public void handleStatement(Statement stmt) throws RDFHandlerException {
        statements.add(stmt);
    }

    @Override
    public RDFFormat getRDFFormat() {
        return writer.getRDFFormat();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        for (Statement stmt : statements) {
            writer.handleStatement(stmt);
        }
        statements.clear();
        writer.endRDF();
    }

    @Override
    public void setWriterConfig(WriterConfig config) {
        writer.setWriterConfig(config);
    }

    @Override
    public WriterConfig getWriterConfig() {
        return writer.getWriterConfig();
    }

    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return writer.getSupportedSettings();
    }

}
