/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.i18n.LocaleContextHolder;

import com.mysema.rdfbean.object.AbstractSessionFactory;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.ObjectRepository;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * @author sasa
 *
 */
public class SesameSessionFactory extends AbstractSessionFactory {
    
	private Configuration defaultConfiguration;

    private AtomicReference<Repository> repositoryRef = new AtomicReference<Repository>();
    
    private Map<String, ObjectRepository> objectRepositories;
    
    private IdentityService identityService;

	@Override
	public Session openSession() {
		return openSession(defaultConfiguration);
	}

	@Override
	public Session openSession(Configuration configuration) {
		Repository repository = repositoryRef.get();
        try {
            SesameSession session = new SesameSession(repository.getConnection(), 
                    LocaleContextHolder.getLocale(),
                    configuration);
            session.setIdentityService(identityService);
            if (objectRepositories != null) {
	            for (Map.Entry<String, ObjectRepository> entry : objectRepositories.entrySet()) {
	            	session.addParent(entry.getKey(), entry.getValue());
	            }
            }
            return session;
        } catch (RepositoryException e) {
        	throw new RuntimeException(e);
		}
	}

	public void setDefaultConfiguration(Configuration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	@Required
	public void setRepository(Repository repository) {
		repositoryRef.set(repository);
	}

	public void setObjectRepositories(Map<String, ObjectRepository> objectRepositories) {
		this.objectRepositories = objectRepositories;
	}

	@Required
	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
	}
        
}
