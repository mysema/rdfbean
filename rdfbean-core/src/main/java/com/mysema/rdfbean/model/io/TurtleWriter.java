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

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public class TurtleWriter implements RDFWriter{

    private final Writer writer;
    
    @Nullable
    private STMT previous;
    
    private final Map<String,String> nsToPrefix = new HashMap<String,String>();
    
    private final Map<BID,String> nodeIds = new HashMap<BID,String>();
    
    public TurtleWriter(OutputStream out) {
        try {
            writer = new OutputStreamWriter(out, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        }
    }
    
    public TurtleWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
    
    @Override
    public void end() {
        try {
            if (previous != null){
                writer.write(" .");    
            }            
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }
    
    @Override
    public void handle(STMT stmt) {
        StringBuilder builder = new StringBuilder();
        if (previous == null && !nsToPrefix.isEmpty()){
            builder.append("\n");
        }
        if (previous == null || !previous.getSubject().equals(stmt.getSubject())){
            if (previous != null){
                builder.append(" .\n\n");    
            }            
            builder.append(toString(stmt));
            
        }else if (previous == null || !previous.getPredicate().equals(stmt.getPredicate())){
            builder.append(" ; ");
            builder.append(toString(stmt.getPredicate()));
            builder.append(" ");
            builder.append(toString(stmt.getObject()));
            
        }else{
            builder.append(" , ");
            builder.append(toString(stmt.getObject()));
        }
        
        try {
            writer.write(builder.toString());
            previous = stmt;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void namespace(String prefix, String namespace) {        
        try {
            nsToPrefix.put(namespace, prefix);
            writer.write("@prefix " + prefix + ": <" + namespace + " > .\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    

    @Override
    public void start() {
        // do nothing
    }

    protected String toString(NODE node){
        if (node.isURI()){
            UID uid = node.asURI();
            if (nsToPrefix.containsKey(uid.ns())){
                return nsToPrefix.get(uid.ns()) + ":" + uid.ln();
            }else{
                return "<" + uid.getId() + ">";
            }
        }else if (node.isBNode()){
            String nodeID = nodeIds.get(node);
            if (nodeID == null){
                nodeID = "node" + (nodeIds.size()+1);
                nodeIds.put(node.asBNode(), nodeID);
            }
            return "_:" + nodeID;
        }else{
            LIT lit = node.asLiteral();
            if (lit.getLang() != null){
                return lit.toString();
            }else{
                return "\"" + lit.getValue() + "\"^" + toString(lit.getDatatype()); 
            }
        }
    }
    
    protected String toString(STMT stmt){
        return toString(stmt.getSubject()) + " " 
            + toString(stmt.getPredicate()) + " " 
            + toString(stmt.getObject());
    }

}
