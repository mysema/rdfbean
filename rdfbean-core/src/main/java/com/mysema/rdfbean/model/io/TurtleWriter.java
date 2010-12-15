package com.mysema.rdfbean.model.io;

import java.util.HashMap;
import java.util.Map;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public final class TurtleWriter {
    
    private final Map<String, String> prefixes = new HashMap<String,String>();
    
    private final StringBuilder prefixesString = new StringBuilder();
    
    private final StringBuilder builder = new StringBuilder();
    
    private STMT last;
    
    public void handle(STMT stmt){
        if (last != null && last.getSubject().equals(stmt.getSubject())){
            builder.append(" ; ");   
        }else{
            if (last != null){
                builder.append(" .\n");    
            }            
            append(stmt.getSubject());
            builder.append(" ");
        }        
        
        append(stmt.getPredicate());
        builder.append(" ");
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
        // TODO : improve
        builder.append("_:b").append(bid.getValue());
    }
    
    private void append(UID uid){
        String prefix = prefixes.get(uid.ns());        
        if (prefix == null){
            prefix = Namespaces.DEFAULT.get(uid.ns());
            if (prefix == null){
                prefix = "ns" + (prefixes.size()+1);
            }
            prefixes.put(uid.ns(), prefix);
            prefixesString.append("@prefix "+prefix+": <"+NTriplesUtil.escapeString(uid.ns())+"> .\n");
        }
        builder.append(prefix).append(":").append(uid.ln());
    }
        
    @Override
    public String toString(){
        builder.insert(0, prefixesString.toString());
        prefixesString.setLength(0);
        return builder.toString();
    }

    public void end() {
        if (last != null){
            builder.append(" .\n");        
        }
    }
}
