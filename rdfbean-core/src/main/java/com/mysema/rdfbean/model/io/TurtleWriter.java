package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class TurtleWriter implements RDFWriter{
    
    private final Writer writer;
    
    private final Map<String, String> prefixes;
    
    public TurtleWriter(Writer writer, Map<String,String> prefixes) {
        this.writer = writer;
        this.prefixes = prefixes;
    }
    
    @Nullable
    private STMT last;
    
    @Override
    public void begin(){
        try{
            for (Map.Entry<String,String> entry : prefixes.entrySet()){
                writer.append("@prefix ");
                writer.append(entry.getValue());
                writer.append(": <");
                writer.append(NTriplesUtil.escapeString(entry.getKey()));
                writer.append("> .\n");
            }
            writer.append("\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }    
    }
    
    @Override
    public void handle(STMT stmt){
        try{
            if (last == null || !last.getSubject().equals(stmt.getSubject())) {
                if (last != null){
                    writer.append(" .\n");    
                }            
                append(stmt.getSubject());
                writer.append(" ");
                append(stmt.getPredicate());
                writer.append(" ");

            } else if (!last.getPredicate().equals(stmt.getPredicate())) {
                writer.append(" ; ");
                append(stmt.getPredicate());
                writer.append(" ");

            } else {
                writer.append(" , ");
            }                

            append(stmt.getObject());           
            last = stmt;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }    
    }
    
    private void append(NODE node) throws IOException {
        if (node.isURI()) {
            append(node.asURI());
        } else if (node.isLiteral()) {
            append(node.asLiteral());
        } else {
            append(node.asBNode());
        }
    }
    
    private void append(LIT lit) throws IOException{
        writer.append("\"");
        writer.append(NTriplesUtil.escapeString(lit.getValue()));
        writer.append("\"");
        if (lit.getLang() != null) {
            writer.append("@").append(LocaleUtil.toLang(lit.getLang()));
        } else {
            writer.append("^^");
            append(lit.getDatatype());
        }
    }
    
    private void append(BID bid) throws IOException{
        writer.append("_:").append(bid.getValue());
    }
    
    private void append(UID uid) throws IOException{
        String prefix = prefixes.get(uid.ns());        
        if (prefix != null){
            writer.append(prefix).append(":").append(uid.ln());    
        }else{
            writer.append("<").append(NTriplesUtil.escapeString(uid.getId())).append(">");
        }
        
    }
        
    @Override
    public void end() {
        if (last != null){
            try {
                writer.append(" .\n");
                writer.flush();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }        
        }
    }

}
