package com.mysema.rdfbean.guice;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.mysema.rdfbean.model.Repository;

/**
 * @author tiwe
 * 
 */
public abstract class RDFBeanRepositoryModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(RDFBeanModule.class);

    public List<String> getConfiguration() {
        return Collections.singletonList("/persistence.properties");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        // inject properties
        try {
            Properties properties = new Properties();
            for (String res : getConfiguration()) {
                properties.load(RDFBeanModule.class.getResourceAsStream(res));
            }
            bind(Properties.class).annotatedWith(Config.class).toInstance(properties);
            for (Map.Entry entry : properties.entrySet()) {
                bind(String.class)
                        .annotatedWith(Names.named(entry.getKey().toString()))
                        .toInstance(entry.getValue().toString());
            }
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }

    }

    @Provides
    @Singleton
    public abstract Repository createRepository(@Config Properties properties);

}
