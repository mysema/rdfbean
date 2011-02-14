/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.tapestry;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TapestryTestRunner.class)
@Modules(ExampleModule.class)
public class ModuleTest {

    @Inject
    private ServiceA serviceA;

    @Inject
    private ServiceB serviceB;

    @Inject
    private ServiceC serviceC;

    @Inject
    private ServiceD serviceD;

    @Test
    public void tx1() {
        serviceA.nonTxMethod();
        serviceA.nonTxMethod2();
        serviceA.txMethod();
        serviceA.txMethod2();
        serviceA.txMethod3();
        serviceA.txReadonly();
    }

    @Test
    public void tx2() {
        serviceB.txMethod();
        serviceB.txReadonly();
        serviceB.nonTxMethod();
    }

    @Test
    public void tx3() {
        serviceC.txMethod();
        serviceC.nonTxMethod();
    }

    @Test
    public void tx4() {
        serviceD.txMethod();
    }

    @Test(expected = Exception.class)
    public void txMethodWithException_commit() throws Exception {
        serviceA.txMethodWithException_commit();
    }

    @Test(expected = Exception.class)
    public void txMethodWithException_rollback() throws Exception {
        serviceA.txMethodWithException_rollback();
    }
}
