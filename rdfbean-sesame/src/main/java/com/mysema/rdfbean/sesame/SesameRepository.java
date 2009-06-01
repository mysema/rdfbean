/**
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

/**
 * @author sasa
 *
 */
public class SesameRepository implements Repository<SesameDialect> {
    
    private org.openrdf.repository.Repository repository;

    public SesameRepository() {}
    
    public SesameRepository(org.openrdf.repository.Repository repository) {
        this.repository = repository;
    }

    @Override
    public RDFConnection openConnection() {
        try {
            return new SesameConnection(repository.getConnection());
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRepository(org.openrdf.repository.Repository repository) {
        this.repository = repository;
    }

}
