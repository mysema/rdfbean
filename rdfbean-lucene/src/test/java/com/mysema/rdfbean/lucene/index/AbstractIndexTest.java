/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.index;

import com.mysema.rdfbean.lucene.AbstractRepositoryTest;
import com.mysema.rdfbean.lucene.LuceneConfiguration;
import com.mysema.rdfbean.lucene.LuceneEnhancedRepository;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractIndexTest extends AbstractRepositoryTest{
    
    @Override
    protected Repository createRepository(LuceneConfiguration configuration){
//        return new LuceneRepository(configuration);
        return new LuceneEnhancedRepository(new MiniRepository(), configuration);
    }

}
