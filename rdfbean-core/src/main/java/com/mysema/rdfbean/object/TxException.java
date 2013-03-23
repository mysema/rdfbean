/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

/**
 * @author tiwe
 */
public class TxException extends SessionException {

    private static final long serialVersionUID = 7761708769507906384L;

    public TxException(Throwable t) {
        super(t);
    }

    public TxException(String msg) {
        super(msg);
    }

    public TxException(String msg, Throwable t) {
        super(msg, t);
    }

}
