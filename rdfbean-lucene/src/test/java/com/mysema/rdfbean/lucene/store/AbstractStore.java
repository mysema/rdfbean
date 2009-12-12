/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.store;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;

import com.mysema.rdfbean.lucene.AbstractRepositoryTest;
import com.mysema.rdfbean.lucene.PropertyConfig;
import com.mysema.rdfbean.lucene.RepositoryMode;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractStore extends AbstractRepositoryTest{

    @Override
    public RepositoryMode getMode(){
        return RepositoryMode.STORE;
    }
    
    @Override
    protected PropertyConfig getDefaultPropertyConfig(){
        return new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, true, 1.0f);
    }
}
