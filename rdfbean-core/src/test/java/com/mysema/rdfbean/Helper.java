package com.mysema.rdfbean;

import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;

public abstract class Helper {

    public static Helper helper = new Helper() {
        @Override
        public Repository createRepository() {
            return new MiniRepository();
        }
    };

    public SessionFactory sessionFactory;

    public Session session;

    public Repository repository;

    public RDFConnection connection;

    public abstract Repository createRepository();

    public Repository newRepository() {
        repository = createRepository();
        repository.initialize();
        return repository;
    }

    public RDFConnection newConnection() {
        connection = repository.openConnection();
        return connection;
    }

    public void closeRepository() {
        if (repository != null) {
            repository.close();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            connection.close();
        }
    }

}
