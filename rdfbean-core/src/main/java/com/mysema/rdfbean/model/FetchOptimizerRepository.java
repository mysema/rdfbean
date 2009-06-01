/**
 * 
 */
package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.List;

import com.mysema.rdfbean.annotations.Required;

/**
 * @author sasa
 *
 */
public class FetchOptimizerRepository implements Repository<MiniDialect> {
    
    private Repository<?> repository;

    private boolean includeInferred = false;
    
    private List<FetchStrategy> fetchStrategies = new ArrayList<FetchStrategy>();
    
    @Override
    public RDFConnection openConnection() {
        return new FetchOptimizer(repository.openConnection(), includeInferred, fetchStrategies);
    }

    @Required
    public void setRepository(Repository<?> repository) {
        this.repository = repository;
    }

    public void setIncludeInferred(boolean includeInferred) {
        this.includeInferred = includeInferred;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

}
