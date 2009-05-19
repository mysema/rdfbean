/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.schema.SchemaGen;

public class SesameSchemaGen extends SchemaGen {

	private IdentityService identityService;
	
	private final Map<String, String> namespaces = new LinkedHashMap<String, String>();
	
	{
		namespaces.put("rdf", RDF.NS);
		namespaces.put("rdfs", RDFS.NS);
		namespaces.put("owl", OWL.NS);
		namespaces.put("xsd", XSD.NS);
	}
	
	public SesameSchemaGen(IdentityService identityService) {
		this.identityService = identityService;
	}

    public void generateTurtle(Configuration configuration, OutputStream out) throws StoreException, RDFHandlerException {
        generateSchema(configuration, new TurtleWriter(out));
    }

    public void generateTurtle(Configuration configuration, Writer out) throws StoreException, RDFHandlerException {
        generateSchema(configuration, new TurtleWriter(out));
    }

    public void generateRDFXML(Configuration configuration, OutputStream out) throws StoreException, RDFHandlerException {
        generateSchema(configuration, new RDFXMLPrettyWriter(out));
    }

    public void generateRDFXML(Configuration configuration, Writer out) throws StoreException, RDFHandlerException {
        generateSchema(configuration, new RDFXMLPrettyWriter(out));
    }
	
	public void generateSchema(Configuration configuration, RDFHandler handler) throws StoreException, RDFHandlerException {
		for (Map.Entry<String, String> entry : namespaces.entrySet()) {
			handler.handleNamespace(entry.getKey(), entry.getValue());
		}
        String ontology = getOntology();
		if (ontology != null && handler instanceof RDFXMLWriter) {
		    RDFXMLWriter writer = (RDFXMLWriter) handler;
		    writer.setBaseURI(ontology);
		}
		handler = new RDFBeanHandler(handler);
	    MemoryStore store = new MemoryStore();
        Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(store));
        repository.initialize();
		
		SesameSessionFactory sessionFactory = new SesameSessionFactory();
		sessionFactory.setIdentityService(identityService);
		sessionFactory.setRepository(repository);
		
		setConfiguration(configuration);
		setSessionFactory(sessionFactory);
		exportConfiguration();
		
		RepositoryConnection conn = repository.getConnection();
		conn.export(handler);
		conn.close();
	}
	
	public SesameSchemaGen setNamespaces(Map<String, String> namespaces) {
		this.namespaces.putAll(namespaces);
		return this;
	}
	
	public SesameSchemaGen setNamespace(String prefix, String namespace) {
		this.namespaces.put(prefix, namespace);
		return this;
	}

    @Override
    public SesameSchemaGen addExportNamespace(String ns) {
        super.addExportNamespace(ns);
        return this;
    }

    @Override
    public SesameSchemaGen addExportNamespaces(Set<String> namespaces) {
        super.addExportNamespaces(namespaces);
        return this;
    }

    @Override
    public SesameSchemaGen setOntology(String ontology) {
        super.setOntology(ontology);
        return this;
    }

    @Override
    public SesameSchemaGen setUseTypedLists(boolean useTypedLists) {
        super.setUseTypedLists(useTypedLists);
        return this;
    }

    @Override
    public SesameSchemaGen setOntologyImports(String... ontologyImports) {
        super.setOntologyImports(ontologyImports);
        return this;
    }
	
}
