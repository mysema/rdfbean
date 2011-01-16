package com.mysema.rdfbean.sparql;

import com.mysema.rdfbean.model.NODE;

/**
 * @author tiwe
 *
 */
public abstract class AbstractResultProducer implements ResultProducer {
    
    protected String getNodeType(NODE node){
        switch (node.getNodeType()) {
            case BLANK:   return "bnode";
            case URI:     return "uri"; 
            case LITERAL: return "literal"; 
        }
        throw new IllegalArgumentException(node.toString());
    }

}
