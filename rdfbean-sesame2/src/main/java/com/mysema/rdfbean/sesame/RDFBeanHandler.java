/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

import com.mysema.rdfbean.CORE;

/**
 * @author sasa
 * 
 */
public class RDFBeanHandler extends RDFHandlerWrapper {

    public RDFBeanHandler(RDFHandler rdfHandler) {
        super(rdfHandler);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        URI predicate = st.getPredicate();
        if (!CORE.NS.equals(predicate.getNamespace()) || !predicate.getLocalName().equals("localId")) {
            super.handleStatement(st);
        }
    }

}
