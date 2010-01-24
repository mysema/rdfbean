/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.Serializable;
import java.util.Comparator;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * StatementComparator provides
 *
 * @author tiwe
 * @version $Id$
 */
@SuppressWarnings("serial")
public class StatementComparator implements Comparator<Statement>, Serializable{
    
    @Override
    public int compare(Statement o1, Statement o2) {
        if (!o1.getSubject().equals(o2.getSubject())){
            return compare(o1.getSubject(), o2.getSubject());
        }else if (!o1.getPredicate().equals(o2.getPredicate())){
            return compare(o1.getPredicate(), o2.getPredicate());
        }else{
            return compare(o1.getObject(), o2.getObject());
        }
    }

    private int compare(Value object, Value object2) {
        int pos1 = getPosition(object);
        int pos2 = getPosition(object2);
        if (pos1 != pos2){
            return pos1 - pos2;
        }else{
            return object.toString().compareTo(object2.toString());    
        }        
    }

    private int getPosition(Value object) {
        if (object instanceof Literal) return 3;
        else if (object instanceof BNode) return 2;
        else return 1;
    }


}
