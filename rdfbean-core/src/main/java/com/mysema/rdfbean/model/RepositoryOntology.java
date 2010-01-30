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
        SetMap<UID,UID> directSubtypes = new SetMap<UID,UID>();
        SetMap<UID, UID> directSupertypes = new SetMap<UID,UID>();
//        SetMap<UID,UID> directSubproperties = new SetMap<UID,UID>();
//        SetMap<UID, UID> directSuperproperties = new SetMap<UID,UID>();
        
        RDFConnection connection = repository.openConnection();
        try{
            // classes
            // TODO : query for RDFS.Class as well
            CloseableIterator<STMT> stmts = connection.findStatements(null, RDF.type, OWL.Class, null, false);
            try{
                while (stmts.hasNext()){
                    STMT stmt = stmts.next();
                    types.add((UID) stmt.getSubject());
                    
                }   
            }finally{
                stmts.close();    
            }
            
            
            // subClassOf hierarchy
            stmts = connection.findStatements(null, RDFS.subClassOf, null, null, false);
            try{
                while (stmts.hasNext()){
                    STMT stmt = stmts.next();
                    if (stmt.getSubject().isURI() && stmt.getObject().isURI()){
                        directSupertypes.put((UID)stmt.getSubject(), (UID)stmt.getObject());
                        directSubtypes.put((UID)stmt.getObject(), (UID)stmt.getSubject());
                    }
                }    
            }finally{
                stmts.close();    
            }
            
            // properties
            // TODO
            
            // subPropertyOf hierarchy
            // TODO
            
            
            initializeTypeHierarchy(types, directSubtypes, directSupertypes);
        }finally{
            connection.close();
        }
    }

}
