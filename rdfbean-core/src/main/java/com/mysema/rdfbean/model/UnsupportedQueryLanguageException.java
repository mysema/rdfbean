/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;


/**
 * UnsupportedQueryLanguageException provides
 *
 * @author tiwe
 * @version $Id$
 */
@SuppressWarnings("serial")
public class UnsupportedQueryLanguageException extends RuntimeException{

    public UnsupportedQueryLanguageException(QueryLanguage<?,?> queryLanguage){
        super("Unsupported query language " + queryLanguage.toString());
    }
}
