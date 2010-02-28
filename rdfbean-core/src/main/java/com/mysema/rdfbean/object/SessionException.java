package com.mysema.rdfbean.object;

/**
 * SessionException provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionException extends RuntimeException{

    private static final long serialVersionUID = 3452448298828566531L;

    public SessionException() {}
    
    SessionException(Throwable t) {
        super(t);
    }
    
    SessionException(String msg){
        super(msg);
    }
    
    SessionException(String msg,Throwable t) {
        super(msg,t);
    }
    
}

