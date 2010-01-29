package com.mysema.rdfbean.guice.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mysema.rdfbean.guice.RDFBeanModule;
import com.mysema.rdfbean.model.Ontology;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.sesame.MemoryRepository;

/**
 * GuiceTestRunner provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class GuiceTestRunner extends BlockJUnit4ClassRunner {
    
    private static final Injector injector = Guice.createInjector(new RDFBeanModule(){
        @Override
        public Repository createRepository(Ontology ontology) {
            return new MemoryRepository(ontology);
        }        
    });
    
    public GuiceTestRunner(Class<?> klass) throws InitializationError  {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        return injector.getInstance(getTestClass().getJavaClass());
    }
}
