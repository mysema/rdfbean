package com.mysema.rdfbean.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author tiwe
 * 
 */
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

    public Bindings getParent() {
        return parent;
    }

    @Nullable
    public NODE get(String key) {
        if (bindings.containsKey(key)) {
            return bindings.get(key);
        } else if (parent != null) {
            return parent.get(key);
        } else {
            return null;
        }
    }

    @Nullable
    public NODE put(String key, NODE node) {
        return bindings.put(key, node);
    }

    public Map<String, NODE> toMap() {
        Map<String, NODE> rv = new HashMap<String, NODE>(bindings);
        if (parent != null) {
            rv.putAll(parent.toMap());
        }
        return rv;
    }

    public Map<String, NODE> toMap(Collection<String> vars) {
        Map<String, NODE> rv = new HashMap<String, NODE>(vars.size());
        for (String var : vars) {
            NODE node = get(var);
            if (node != null) {
                rv.put(var, node);
            }
        }
        return rv;
    }

    @Override
    public String toString() {
        if (parent != null) {
            return parent + " " + bindings;
        } else {
            return bindings.toString();
        }

    }
}
