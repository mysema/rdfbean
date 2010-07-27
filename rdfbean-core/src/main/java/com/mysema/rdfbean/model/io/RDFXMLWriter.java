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
// TODO : consider using fluxml
public class RDFXMLWriter implements RDFWriter{

    private final Writer writer;
    
    private final Map<String,String> nsToPrefix = new HashMap<String,String>(); 
    
    private final Map<BID,String> nodeIds = new HashMap<BID,String>();
    
    private STMT previous;
    
    public RDFXMLWriter(OutputStream out) {
        try {
            writer = new OutputStreamWriter(out, "UTF-8");
            nsToPrefix.put(RDF.NS,"rdf");
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        }
    }
    
    public RDFXMLWriter(Writer writer) {
        this.writer = writer;
        nsToPrefix.put(RDF.NS,"rdf");
    }
    
    
    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    @Override
    public void end() {
        try {
            if (previous == null){
                writer.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
            }
            writer.write("</rdf:RDF>");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    @Override
    public void handle(STMT stmt) {
        StringBuilder builder = new StringBuilder();
        // prolog
        if (previous == null){
            builder.append("<rdf:RDF ");
            for (Map.Entry<String,String> entry : nsToPrefix.entrySet()){
                if (builder.length() > 9){
                    builder.append("\n    ");
                }
                builder.append("xmlns:"+entry.getValue()+"=\""+entry.getKey()+"\"");
            }
            builder.append(">\n\n");
        }        
        
        // subject
        if (stmt.getSubject().isBNode()){
            builder.append("    <rdf:Description rdf:nodeID=\""+ getNodeID(stmt.getSubject().asBNode())+"\">\n");   
        }else{
            builder.append("    <rdf:Description rdf:about=\""+stmt.getSubject().getId()+"\">\n");    
        }     
        
        // predicate
        String prefix = nsToPrefix.get(stmt.getPredicate().ns());
        if (prefix != null){
            builder.append("        <"+prefix+":"+stmt.getPredicate().ln());
        }else{
            // TODO
        }
        
        // object
        if (stmt.getObject().isBNode()){
            builder.append(" rdf:nodeID=\"" + getNodeID(stmt.getObject().asBNode()) + "\"/>\n");
        }else if (stmt.getObject().isURI()){
            builder.append(" rdf:resource=\"" + stmt.getObject().getValue() + "\"/>\n");
        }else{
            LIT obj = stmt.getObject().asLiteral();
            if (obj.getLang() != null){
                builder.append(" xml:lang=\"" + LocaleUtil.toLang(obj.getLang())+ "\"");
            }else if (!obj.getDatatype().equals(XSD.stringType)){
                builder.append(" rdf:datatype=\"" + obj.getDatatype().getId() + "\"");
            }
            builder.append(">" + stmt.getObject().getValue() + "</" + prefix + ":" + stmt.getPredicate().ln()+">\n");
        }
        builder.append("    </rdf:Description>\n\n");
        
        try {
            writer.write(builder.toString());
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
