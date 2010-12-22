package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;

public class SPARQLUpdateWriter extends TurtleStringWriter{
    
    private final Map<String, String> prefixes = new HashMap<String,String>();
    
    private final StringBuilder prefixesString = new StringBuilder();
    
    private final boolean delete;
    
    private final UID graph;
    
    public SPARQLUpdateWriter(UID graph, boolean delete) {
        this(graph, false, false);
    }
        
    public SPARQLUpdateWriter(UID graph, boolean delete, boolean blankNodeAsURI) {
        super(blankNodeAsURI); 
        this.graph = graph;
        this.delete = delete;
    }
        
    @Override
    protected void append(UID uid){
        String prefix = prefixes.get(uid.ns());        
        if (prefix == null){
            prefix = Namespaces.DEFAULT.get(uid.ns());
            if (prefix == null){
                prefix = "ns" + (prefixes.size()+1);
            }
            prefixes.put(uid.ns(), prefix);
            prefixesString.append("PREFIX "+prefix+": <"+NTriplesUtil.escapeString(uid.ns())+">\n");
        }
        try{
            appendable.append(prefix).append(":").append(uid.ln());        
        } catch (IOException e) {
            throw new RepositoryException(e);
        }       
    }
    
    @Override
    protected void appendPredicate(UID uid){
        append(uid);
    }
    
    @Override
    public void begin(){
        try{
            if (delete){
                appendable.append("DELETE DATA FROM <").append(graph.getId()).append("> {\n");    
            }else{
                appendable.append("INSERT DATA INTO <").append(graph.getId()).append("> {\n");   
            }   
        } catch (IOException e) {
            throw new RepositoryException(e);
        }   
    }
        
    @Override
    public void end() {
        try{
            if (last != null){
                appendable.append(" .\n");        
            }
            appendable.append("}\n");    
        } catch (IOException e) {
            throw new RepositoryException(e);
        }           
    }

    @Override
    public String toString(){
        ((StringBuilder)appendable).insert(0, prefixesString.toString());
        prefixesString.setLength(0);
        return appendable.toString();
    }

}
