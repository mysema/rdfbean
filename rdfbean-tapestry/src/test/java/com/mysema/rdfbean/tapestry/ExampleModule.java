package com.mysema.rdfbean.tapestry;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;

import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.sesame.MemoryRepository;
import com.mysema.rdfbean.tapestry.services.RDFBeanModule;

@SubModule( { RDFBeanModule.class })
public class ExampleModule {

    @Match({ "ServiceA", "ServiceB", "ServiceC", "ServiceD" })
    public static void adviseTransactions(TransactionalAdvisor advisor, MethodAdviceReceiver receiver) {
        advisor.addTransactionCommitAdvice(receiver);
    }

    public static void bind(ServiceBinder binder) {
        binder.bind(ServiceA.class, ServiceAImpl.class);
        binder.bind(ServiceB.class, ServiceBImpl.class);
        binder.bind(ServiceC.class, ServiceCImpl.class);
        binder.bind(ServiceD.class, ServiceDImpl.class);
    }

    public static Configuration buildConfiguration() {
        DefaultConfiguration configuration = new DefaultConfiguration();
        return configuration;
    }

    public static Repository buildRepository(RegistryShutdownHub hub) {
        final MemoryRepository repository = new MemoryRepository();
        hub.addRegistryShutdownListener(new RegistryShutdownListener() {
            @Override
            public void registryDidShutdown() {
                repository.close();
            }
        });
        return repository;
    }
}
