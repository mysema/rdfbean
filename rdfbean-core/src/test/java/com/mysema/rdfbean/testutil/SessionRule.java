package com.mysema.rdfbean.testutil;

import java.lang.reflect.Field;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

public class SessionRule implements MethodRule{
    
    private final Repository repository;
    
    public SessionRule(Repository repository) {
        this.repository = repository;
    }

    @Override
    public Statement apply(final Statement base, FrameworkMethod method, final Object target) {
        final TestConfig config = target.getClass().getAnnotation(TestConfig.class);
        if (config != null){
            return new Statement(){
                @Override
                public void evaluate() throws Throwable {
                    Session session = SessionUtil.openSession(repository, config.value());
                    RDFBeanTransaction tx = session.beginTransaction();                   
                    try{
                        Field field = target.getClass().getDeclaredField("session");
                        field.setAccessible(true);
                        field.set(target, session);
                        base.evaluate();    
                    }finally{
                        tx.rollback();
                        session.close();
                    }
                    
                }                
            };
        }else{
            return base;
        }
    }

}
