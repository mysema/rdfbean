/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;
import java.util.Locale;

import com.mysema.rdfbean.model.*;

/**
 * @author sasa
 *
 */
public class MiniSession extends AbstractSession<NODE, ID, BID, UID, LIT, STMT> {
    
    private final MiniDialect dialect;
    
    private final MiniRepository repository;
    
    public MiniSession(Class<?>... classes) {
        this(new MiniRepository(), classes);
    }

    public MiniSession(Configuration ctx) {
        this(new MiniRepository(), ctx);
    }

    public MiniSession(List<Locale> locales, Class<?>... classes) {
        this(new MiniRepository(), locales, classes);
    }

    public MiniSession(List<Locale> locales, Configuration defaultCtx) {
        this(new MiniRepository(), locales, defaultCtx);
    }

    public MiniSession(List<Locale> locales, Package... packages) throws ClassNotFoundException {
        this(new MiniRepository(), locales, packages);
    }

    public MiniSession(Locale locale, Class<?>... classes) {
        this(new MiniRepository(), locale, classes);
    }

    public MiniSession(Locale locale, Configuration ctx) {
        this(new MiniRepository(), locale, ctx);
    }

    public MiniSession(Locale locale, Package... packages) throws ClassNotFoundException {
        this(new MiniRepository(), locale, packages);
    }

    public MiniSession(MiniRepository repository, Class<?>... classes) {
        super(classes);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }
    
    public MiniSession(MiniRepository repository, Configuration ctx) {
        super(ctx);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, List<Locale> locales, Class<?>... classes) {
        super(locales, classes);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, List<Locale> locales, Configuration defaultCtx) {
        super(locales, defaultCtx);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, List<Locale> locales, Package... packages) throws ClassNotFoundException {
        super(locales, packages);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, Locale locale, Class<?>... classes) {
        super(locale, classes);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, Locale locale, Configuration ctx) {
        super(locale, ctx);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, Locale locale, Package... packages) throws ClassNotFoundException {
        super(locale, packages);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(MiniRepository repository, Package... packages) throws ClassNotFoundException {
        super(packages);
        this.repository = repository;
        this.dialect = repository.getDialect();
    }

    public MiniSession(Package... packages) throws ClassNotFoundException {
        this(new MiniRepository(), packages);
    }
    
    @Override
    protected void addStatement(STMT stmt, UID context) {
        repository.add(stmt);
    }

    @Override
    public RDFBeanTransaction beginTransaction() {
        throw new UnsupportedOperationException("No transactional support");
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        throw new UnsupportedOperationException("No transactional support");
    }
    
    @Override
    public void close() {
    }

    @Override
    protected List<STMT> findStatements(ID subject, UID predicate, NODE object, boolean includeInferred, UID context) {
        return repository.findStatements(subject, predicate, object);
    }

    @Override
    public Dialect<NODE, ID, BID, UID, LIT, STMT> getDialect() {
        return dialect;
    }

    public MiniRepository getRepository() {
        return repository;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    protected void removeStatement(STMT statement, UID context) {
        repository.removeStatement(statement);
    }

}
