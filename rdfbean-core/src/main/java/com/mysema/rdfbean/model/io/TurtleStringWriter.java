package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.UID;

public class TurtleStringWriter extends TurtleWriter {
    
    private final Map<String, String> prefixes = new HashMap<String,String>();
    
    private final StringBuilder prefixesString = new StringBuilder();
    
    public TurtleStringWriter() {
        this(false);
    }
        
    public TurtleStringWriter(boolean blankNodeAsURI) {
        super(new StringBuilder(), Collections.<String,String>emptyMap(), blankNodeAsURI);
    }
            
    @Override
    protected void appendPrefixed(UID uid) throws IOException{
        String prefix = prefixes.get(uid.ns());        
        if (prefix == null){
            prefix = Namespaces.DEFAULT.get(uid.ns());
            if (prefix == null){
                prefix = "ns" + (prefixes.size()+1);
            }
            prefixes.put(uid.ns(), prefix);
            prefixesString.append("@prefix "+prefix+": <"+NTriplesUtil.escapeString(uid.ns())+"> .\n");
        }
        appendable.append(prefix).append(":").append(uid.ln());
    }
        
    @Override
    public String toString(){
        ((StringBuilder)appendable).insert(0, prefixesString.toString());
        prefixesString.setLength(0);
        return appendable.toString();
    }

}
