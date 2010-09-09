#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package ${package}.testutil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * TapestryTestRunner provides Tapestry IoC injection functionality for tests
 */
public class TapestryTestRunner extends BlockJUnit4ClassRunner {
    
    private static final Map<Set<Class<?>>, Registry> registries = new HashMap<Set<Class<?>>,Registry>();
            
    public TapestryTestRunner(Class<?> klass) throws org.junit.runners.model.InitializationError {
        super(klass);
    }

    private static Registry getRegistry(Class<?> testClass){
        Class<?>[] classes = testClass.getAnnotation(Modules.class).value();
        Set<Class<?>> modules = new HashSet<Class<?>>(Arrays.asList(classes));
        Registry registry;
        if (!registries.containsKey(modules)){
            registry = new RegistryBuilder().add(classes).build();    
            registry.performRegistryStartup();
            registries.put(modules, registry);
        }else{
            registry = registries.get(modules);
        }
        return registry;
    }
    
    @Override
    protected Object createTest() throws Exception {
        Class<?> testClass = getTestClass().getJavaClass();        
        return getRegistry(testClass).autobuild(testClass);
    }
}
