package com.mysema.rdfbean.spring;

import org.springframework.transaction.annotation.Transactional;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.object.SessionFactory;

public class DemoService {

    private SessionFactory sessionFactory;

    @Transactional
    public void assertWriteTx() {
        Assert.notNull(sessionFactory.getCurrentSession(), "session.currentSession");
    }

    @Transactional(readOnly=true)
    public void assertReadTx() {
        Assert.notNull(sessionFactory.getCurrentSession(), "session.currentSession");
    }

    public void assertUnbound() {
        Assert.isTrue(sessionFactory.getCurrentSession() == null, "session is bound");        
    }
    
    @Transactional(rollbackFor=Exception.class)
    public void rollback() throws Exception{
        throw new Exception();
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


}
