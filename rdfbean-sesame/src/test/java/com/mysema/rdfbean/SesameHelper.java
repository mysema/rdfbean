package com.mysema.rdfbean;

import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SesameHelper extends Helper{

    @Override
    public Repository createRepository() {
        return new MemoryRepository();
    }

}
