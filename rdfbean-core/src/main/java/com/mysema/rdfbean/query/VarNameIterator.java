/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mysema.commons.lang.Assert;

/**
 * VarNameIterator is an Iterator implementation to create var names for queries
 *
 * @author tiwe
 * @version $Id$
 */
public class VarNameIterator implements Iterator<String>{

    private int counter;
    private Set<String> disallowed = new HashSet<String>();
    private char firstChar = 'a';
    
    private String prefix = null;
    
    public VarNameIterator(){}
    
    public VarNameIterator(String prefix){
        this.prefix = Assert.notNull(prefix);
    }
    
    public void disallow(String str){
        disallowed.add(str);
    }    
    
    public boolean hasNext() {
        return true;
    }
    
    private String produceNext(){
        StringBuilder rv;
        if (prefix != null){
            rv = new StringBuilder(prefix.length()+3).append(prefix);
        }else{
            rv = new StringBuilder(3);
        }
        rv.append(firstChar);
        if (counter > 0){
            rv.append(counter);
        }
        if (firstChar == 'z'){
            firstChar = 'a';
            counter++;
        }else{
            firstChar++;
        }
        return rv.toString();
    }
    
    public String next() {        
        String rv;
        do{
            rv = produceNext();
        }while(disallowed.contains(rv));
        return rv;
    }
    
    public void remove() {
        
    }

}
