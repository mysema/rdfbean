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

import com.mysema.rdfbean.model.RepositoryException;

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

        InferencerConnection conn = getConnection();
        try {
            conn.begin();
            for (Field field : XMLSchema.class.getFields()){
                if (field.getType().equals(URI.class)){
                    conn.addInferredStatement((URI)field.get(null), RDF.TYPE, RDFS.DATATYPE);
                }
            }
            // TODO : datatype relations
            conn.commit();
        } catch (StoreException e) {
            conn.rollback();
            String error = "Caught " + e.getClass().getName();
            throw new RepositoryException(error, e);
        } catch (IllegalAccessException e) {
            conn.rollback();
            String error = "Caught " + e.getClass().getName();
            throw new RepositoryException(error, e);
        } finally {
            conn.close();
        }
    }
}
