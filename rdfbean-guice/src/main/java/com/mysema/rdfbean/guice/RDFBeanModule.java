package com.mysema.rdfbean.guice;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.object.identity.DerbyIdentityService;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * RDFBeanModule provides
 *
 * @author tiwe
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public abstract class RDFBeanModule extends AbstractModule{

    private static final Logger logger = LoggerFactory.getLogger(RDFBeanModule.class);
    
    @Override
    protected void configure() {               
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/persistence.properties"));
            for (Object key : properties.keySet()){
                bind(String.class)
                    .annotatedWith(Names.named(key.toString()))
                    .toInstance(properties.getProperty(key.toString()));
            }
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
       
        RDFBeanTxnInterceptor interceptor = new RDFBeanTxnInterceptor();
        requestInjection(interceptor);
        bindInterceptor(
                Matchers.any(), 
                Matchers.annotatedWith(Transactional.class),
                interceptor);
    }
    
    @Provides
    @Singleton
    public IdentityService identityService(@Named("identityService.derby.url") String url) throws IOException{
        return new DerbyIdentityService(url);
    }
    
    @Provides 
    @Singleton
    public abstract Repository repository();
    
    @Provides
    @Singleton
    public Configuration configuration(IdentityService identityService){
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.setIdentityService(identityService);
        return configuration;
    }
    
    @Provides
    @Singleton
    public SessionFactory sessionFactory(Configuration configuration, Repository repository){        
        // TODO : locale handling
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }

}
