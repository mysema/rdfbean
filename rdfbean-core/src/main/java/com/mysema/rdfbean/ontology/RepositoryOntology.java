/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.util.MultiMapFactory;


/**
 * RepositoryOntology provides a repository based implementation of the Ontology interface
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryOntology extends AbstractOntology<UID>{
    
    public RepositoryOntology(Repository repository) throws IOException{
        Set<UID> types = new HashSet<UID>();
        Set<UID> properties = new HashSet<UID>();             
        RDFConnection connection = repository.openConnection();
        try{
            // types
            getInstances(types, RDFS.Class, connection);
            getInstances(types, OWL.Class, connection);                        
            initTypeHierachy(types, connection);
            
            // properties
            getInstances(properties, RDF.Property, connection);
            getInstances(properties, OWL.ObjectProperty, connection);
            getInstances(properties, OWL.DatatypeProperty, connection);
            initPropertyHierarchy(properties, connection);
        }finally{
            connection.close();
        }
    }
    
    private void getInstances(Set<UID> instances, UID type, RDFConnection connection)throws IOException {
        CloseableIterator<STMT> stmts = connection.findStatements(null, RDF.type, type, null, false);
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                if (stmt.getSubject().isURI()){
                    instances.add(stmt.getSubject().asURI());    
                }                                
            }   
        }finally{
            stmts.close();    
        }
    }

    private void initTypeHierachy(Set<UID> types, RDFConnection connection) throws IOException {
        MultiMap<UID,UID> directSubtypes = MultiMapFactory.<UID,UID>createWithSet();
        MultiMap<UID, UID> directSupertypes = MultiMapFactory.<UID,UID>createWithSet();
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
    
    private void initPropertyHierarchy(Set<UID> properties, RDFConnection connection) throws IOException {
        MultiMap<UID,UID> directSubproperties = MultiMapFactory.<UID,UID>createWithSet();
        MultiMap<UID, UID> directSuperproperties = MultiMapFactory.<UID,UID>createWithSet();
        CloseableIterator<STMT> stmts = connection.findStatements(null, RDFS.subPropertyOf, null, null, false);
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                if (stmt.getSubject().isURI() && stmt.getObject().isURI()){
                    directSuperproperties.put((UID)stmt.getSubject(), (UID)stmt.getObject());
                    directSubproperties.put((UID)stmt.getObject(), (UID)stmt.getSubject());
                }
            }
            initializePropertyHierarchy(properties, directSubproperties, directSuperproperties);
        }finally{
            stmts.close();    
        }
    }



}
