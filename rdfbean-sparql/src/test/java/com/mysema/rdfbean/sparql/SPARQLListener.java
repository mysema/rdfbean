package com.mysema.rdfbean.sparql;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLListener implements ServletContextListener{

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Repository repository = (Repository)sce.getServletContext().getAttribute(Repository.class.getName());
        if (repository != null){
            repository.close();
        }
        
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MemoryRepository repository = new MemoryRepository();
        repository.initialize();
        repository.load(Format.RDFXML, getClass().getResourceAsStream("/foaf.rdf"), null, false);
        sce.getServletContext().setAttribute(Repository.class.getName(), repository);
    }

}
