/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tiwe
 */
public abstract class AbstractDialect
    <N,
     R extends N,
     B extends R,
     U extends R,
     L extends N,
     S> implements Dialect<N, R, B, U, L, S> {

    private final Map<String,UID> datatypeUIDCache = new HashMap<String,UID>();

    public AbstractDialect(){
        for (UID uid : Nodes.get(XSD.NS)){
            datatypeUIDCache.put(uid.getId(), uid);
        }
    }

    protected UID getDatatypeUID(String datatype){
        UID uid = datatypeUIDCache.get(datatype);
        if (uid == null){
            uid = new UID(datatype);
            datatypeUIDCache.put(datatype, uid);
        }
        return uid;
    }

    @Override
    public N getNode(NODE node){
        if (node.isLiteral()){
            return getLiteral(node.asLiteral());
        }else if (node.isBNode()){
            return getBNode(node.asBNode());
        }else{
            return getURI(node.asURI());
        }
    }

    @Override
    public R getResource(ID id) {
        if (id.isURI()){
            return getURI(id.asURI());
        }else{
            return getBNode(id.asBNode());
        }
    }

}
