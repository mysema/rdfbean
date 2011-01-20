/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * @author tiwe
 */
public class SessionException extends RuntimeException{

    private static final long serialVersionUID = 3452448298828566531L;

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

