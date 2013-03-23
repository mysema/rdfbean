package com.mysema.rdfbean.testutil;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class PrintTestName implements MethodRule {

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                System.err.println(method.getName());
                base.evaluate();
            }
        };
    }

}
