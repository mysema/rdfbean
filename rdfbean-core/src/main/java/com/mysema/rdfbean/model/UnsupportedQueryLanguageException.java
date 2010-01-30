/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.rdfbean.object.QueryLanguage;

/**
 * UnsupportedQueryLanguageException provides
 *
 * @author tiwe
 * @version $Id$
 */
@SuppressWarnings("serial")
public class UnsupportedQueryLanguageException extends RuntimeException{

    public UnsupportedQueryLanguageException(QueryLanguage<?,?> queryLanguage){
        super("Unsupported query language " + queryLanguage.getName());
    }
}
