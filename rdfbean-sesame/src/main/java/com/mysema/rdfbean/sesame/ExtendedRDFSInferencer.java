/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.lang.reflect.Field;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.store.StoreException;

/**
 * ExtendedRDFSInferencer extends ForwardChainingRDFSInferencer to add
 * better support for XSD datatype instances
 * 
 * @author tiwe
 * @version $Id$
 */
public class ExtendedRDFSInferencer extends ForwardChainingRDFSInferencer {

    public ExtendedRDFSInferencer() {
        super();
    }

    public ExtendedRDFSInferencer(NotifyingSail baseSail) {
        super(baseSail);
    }

    @Override
    public void initialize() throws StoreException {
        super.initialize();

        InferencerConnection con = getConnection();
        try {
            con.begin();
            for (Field field : XMLSchema.class.getFields()){
                if (field.getType().equals(URI.class)){
                    con.addInferredStatement((URI)field.get(null), RDF.TYPE, RDFS.DATATYPE);
                }
            }
            // TODO : datatype relations
            con.commit();
        } catch (Exception e) {
            con.rollback();
            String error = "Caught " + e.getClass().getName();
            throw new RuntimeException(error, e);
        } finally {
            con.close();
        }
    }
}
