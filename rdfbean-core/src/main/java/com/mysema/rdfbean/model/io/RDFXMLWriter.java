/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.fluxml.XMLWriter;
import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.XSD;

/**
 * @author tiwe
 *
 */
public class RDFXMLWriter implements RDFWriter{

    private static final String RDF_ABOUT = "rdf:about";

    private static final String RDF_DATATYPE = "rdf:datatype";

    private static final String RDF_DESCRIPTION = "rdf:Description";

    private static final String RDF_NODE_ID = "rdf:nodeID";
    
    private static final String RDF_RDF = "rdf:RDF";
    
    private static final String RDF_RESOURCE = "rdf:resource";

    private static final String XML_LANG = "xml:lang";
    
    private final Writer w;
    
    private final XMLWriter writer;
    
    private final Map<String,String> nsToPrefix = new HashMap<String,String>(); 
    
    private final Map<BID,String> nodeIds = new HashMap<BID,String>();
    
    @Nullable
    private STMT previous;
    
    public RDFXMLWriter(OutputStream out) {
        try {
            w = new OutputStreamWriter(out, "UTF-8");
            writer = new XMLWriter(w);
            nsToPrefix.put(RDF.NS,"rdf");
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        }
    }
    
    public RDFXMLWriter(Writer writer) {
        this.w = writer;
        this.writer = new XMLWriter(w);
        nsToPrefix.put(RDF.NS,"rdf");
    }
    
    
    @Override
    public void close() throws IOException {
        w.flush();
        w.close();
    }

    @Override
    public void end() {
        try {
            if (previous == null){                
                writer.begin(RDF_RDF).attribute("xmlns:rdf", RDF.NS);
            }
            writer.end(RDF_RDF);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    @Override
    public void handle(STMT stmt) {
        try {
            // prolog
            if (previous == null){
                writer.begin(RDF_RDF);
                for (Map.Entry<String,String> entry : nsToPrefix.entrySet()){
                    writer.attribute("xmlns:"+ entry.getValue(), entry.getKey());
                }
            }        

            // subject
            writer.begin(RDF_DESCRIPTION);
            if (stmt.getSubject().isBNode()){
                writer.attribute(RDF_NODE_ID, getNodeID(stmt.getSubject().asBNode()));   
            }else{
                writer.attribute(RDF_ABOUT, stmt.getSubject().getId());    
            }     

            // predicate
            String prefix = nsToPrefix.get(stmt.getPredicate().ns());
            if (prefix != null){
                writer.begin(prefix+":"+stmt.getPredicate().ln());
            }else{
                throw new IllegalStateException("No prefix for " + stmt.getPredicate().ns());
            }

            // object
            if (stmt.getObject().isBNode()){
                writer.attribute(RDF_NODE_ID, getNodeID(stmt.getObject().asBNode()));
            }else if (stmt.getObject().isURI()){
                writer.attribute(RDF_RESOURCE, stmt.getObject().getValue());
            }else{
                LIT obj = stmt.getObject().asLiteral();
                if (obj.getLang() != null){
                    writer.attribute(XML_LANG, LocaleUtil.toLang(obj.getLang()));
                }else if (!obj.getDatatype().equals(XSD.stringType)){
                    writer.attribute(RDF_DATATYPE, obj.getDatatype().getId());
                }
                writer.print(stmt.getObject().getValue());                
            }
            writer.end(prefix +":"+stmt.getPredicate().ln());
            writer.end(RDF_DESCRIPTION);
            previous = stmt;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    private String getNodeID(BID node) {
        String nodeID = nodeIds.get(node);
        if (nodeID == null){
            nodeID = "node" + (nodeIds.size()+1);
            nodeIds.put(node, nodeID);
        }
        return nodeID;
    }

    @Override
    public void namespace(String prefix, String namespace) {
        nsToPrefix.put(namespace, prefix);
    }

    @Override
    public void start() {
        // do nothing        
    }


}
