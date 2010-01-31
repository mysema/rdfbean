/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.util.SetMap;


/**
 * RepositoryOntology provides a repository based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryOntology extends AbstractOntology{
    
    public RepositoryOntology(Repository repository) throws IOException{
        Set<UID> types = new HashSet<UID>();                
        RDFConnection connection = repository.openConnection();
        try{
            initTypes(types, connection);                        
            initTypeHierachy(types, connection);  
            // TODO : initProperties
            // TODO : initPropertyHierarchy
        }finally{
            connection.close();
        }
    }

    private void initTypeHierachy(Set<UID> types, RDFConnection connection) throws IOException {
        SetMap<UID,UID> directSubtypes = new SetMap<UID,UID>();
        SetMap<UID, UID> directSupertypes = new SetMap<UID,UID>();
        CloseableIterator<STMT> stmts = connection.findStatements(null, RDFS.subClassOf, null, null, false);
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                if (stmt.getSubject().isURI() && stmt.getObject().isURI()){
                    directSupertypes.put((UID)stmt.getSubject(), (UID)stmt.getObject());
                    directSubtypes.put((UID)stmt.getObject(), (UID)stmt.getSubject());
                }
            }
            initializeTypeHierarchy(types, directSubtypes, directSupertypes);
        }finally{
            stmts.close();    
        }
    }

    private void initTypes(Set<UID> types, RDFConnection connection)throws IOException {
        CloseableIterator<STMT> stmts = connection.findStatements(null, RDF.type, OWL.Class, null, false);
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                types.add((UID) stmt.getSubject());
                
            }   
        }finally{
            stmts.close();    
        }
    }

}
