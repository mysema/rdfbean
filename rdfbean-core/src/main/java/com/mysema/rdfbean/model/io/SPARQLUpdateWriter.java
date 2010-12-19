package com.mysema.rdfbean.model.io;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class SPARQLUpdateWriter implements RDFWriter{
    
    private final Map<String, String> prefixes = new HashMap<String,String>();
    
    private final StringBuilder prefixesString = new StringBuilder();
    
    private final StringBuilder builder = new StringBuilder();
    
    private final UID graph;
    
    private final boolean delete;
    
    private final boolean blankNodeAsURI;

    public SPARQLUpdateWriter(UID graph, boolean delete) {
        this(graph, false, false);
    }
        
    public SPARQLUpdateWriter(UID graph, boolean delete, boolean blankNodeAsURI) {
        this.graph = graph;
        this.delete = delete;
        this.blankNodeAsURI = blankNodeAsURI;
    }
    
    @Nullable
    private STMT last;
    
    @Override
    public void begin(){
        if (delete){
            builder.append("DELETE DATA FROM <").append(graph.getId()).append("> {\n");    
        }else{
            builder.append("INSERT DATA INTO <").append(graph.getId()).append("> {\n");   
        }        
    }
    
    @Override
    public void handle(STMT stmt){
        if (last == null || !last.getSubject().equals(stmt.getSubject())) {
            if (last != null){
                builder.append(" .\n");    
            }            
            append(stmt.getSubject());
            builder.append(" ");
            append(stmt.getPredicate());
            builder.append(" ");
            
        } else if (!last.getPredicate().equals(stmt.getPredicate())) {
            builder.append(" ; ");
            append(stmt.getPredicate());
            builder.append(" ");
                        
        } else {
            builder.append(" , ");
        }                
        
        append(stmt.getObject());           
        last = stmt;
    }
    
    private void append(NODE node) {
        if (node.isURI()) {
            append(node.asURI());
        } else if (node.isLiteral()) {
            append(node.asLiteral());
        } else {
            append(node.asBNode());
        }
    }
    
    private void append(LIT lit){
        builder.append("\"");
        builder.append(NTriplesUtil.escapeString(lit.getValue()));
        builder.append("\"");
        if (lit.getLang() != null) {
            builder.append("@").append(LocaleUtil.toLang(lit.getLang()));
        } else {
            builder.append("^^");
            append(lit.getDatatype());
        }
    }
    
    private void append(BID bid){
        if (blankNodeAsURI){
            builder.append("<_:").append(bid.getValue()).append(">");
        }else{
            builder.append("_:").append(bid.getValue());    
        }
        
    }
    
    private void append(UID uid){
        String prefix = prefixes.get(uid.ns());        
        if (prefix == null){
            prefix = Namespaces.DEFAULT.get(uid.ns());
            if (prefix == null){
                prefix = "ns" + (prefixes.size()+1);
            }
            prefixes.put(uid.ns(), prefix);
            prefixesString.append("PREFIX "+prefix+": <"+NTriplesUtil.escapeString(uid.ns())+">\n");
        }
        builder.append(prefix).append(":").append(uid.ln());
    }
        
    @Override
    public String toString(){
        builder.insert(0, prefixesString.toString());
        prefixesString.setLength(0);
        return builder.toString();
    }

    @Override
    public void end() {
        if (last != null){
            builder.append(" .\n");        
        }
        builder.append("}\n");
    }

}
