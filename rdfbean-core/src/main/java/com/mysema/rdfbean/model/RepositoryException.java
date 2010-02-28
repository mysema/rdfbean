package com.mysema.rdfbean.model;

/**
 * RepositoryException provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryException extends RuntimeException{

    private static final long serialVersionUID = 2825073196525806341L;

    public RepositoryException() {}
    
    public RepositoryException(String msg){
        super(msg);
    }
    
    public RepositoryException(Throwable t) {
        super(t);
    }
    
    public RepositoryException(String msg, Throwable t) {
        super(msg,t);
    }
    
}

