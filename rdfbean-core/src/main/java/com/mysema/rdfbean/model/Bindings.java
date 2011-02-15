package com.mysema.rdfbean.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Bindings {

    private final Map<String, NODE> bindings = new HashMap<String, NODE>();

    @Nullable
    private final Bindings parent;

    public Bindings() {
        this.parent = null;
    }

    public Bindings(Bindings parent) {
        this.parent = parent;
    }

    public void clear() {
        bindings.clear();
    }

    @Nullable
    public NODE get(String key){
        if (bindings.containsKey(key)){
            return bindings.get(key);
        }else if (parent != null){
            return parent.get(key);
        }else{
            return null;
        }
    }

    @Nullable
    public NODE put(String key, NODE node){
        return bindings.put(key, node);
    }

    public Map<String, NODE> toMap(){
        if (parent != null){
            Map<String, NODE> rv = new HashMap<String, NODE>(bindings);
            rv.putAll(parent.toMap());
            return rv;
        }else{
            return bindings;
        }
    }

}
