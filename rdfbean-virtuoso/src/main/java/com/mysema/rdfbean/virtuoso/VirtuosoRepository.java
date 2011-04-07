package com.mysema.rdfbean.virtuoso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.model.io.RDFWriter;
import com.mysema.rdfbean.model.io.WriterUtils;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * @author tiwe
 *
 */
public class VirtuosoRepository implements Repository {

    private static final String RDFBEAN_NIL = "rdfbean:nil";

    private static final Logger logger = LoggerFactory.getLogger(VirtuosoRepository.class);

    private final VirtuosoConnectionPoolDataSource pds = new VirtuosoConnectionPoolDataSource();

    private final String host;

    private final Converter converter = new Converter(new ConverterRegistryImpl());

    @Nullable
    private RDFSource[] sources;

    @Nullable
    private File dataDir;

    private static final String charset = "UTF-8";

    private final UID defGraph;

    private Collection<UID> allowedGraphs = Collections.emptySet();

    private int prefetchSize = 200;

    private boolean initialized = false;

    @Nullable
    private Integer initialPoolSize, minPoolSize, maxPoolSize;

    @Nullable
    private IdSequence idSequence;

    public VirtuosoRepository(String hostlist, String user, String password) {
        this(hostlist, user, password, RDFBEAN_NIL);
    }

    public VirtuosoRepository(String host, int port, String user, String password) {
        this(host, port, user, password, RDFBEAN_NIL);
    }

    public VirtuosoRepository(String hostlist, String user, String password, String defGraph) {
        this(hostlist, 1111, user, password, defGraph);
    }

    public VirtuosoRepository(String host, int port, String user, String password, String defGraph) {
        this.defGraph = new UID(defGraph);
        this.host = host;
        pds.setServerName(host);
        pds.setPortNumber(port);
        pds.setUser(user);
        pds.setPassword(password);
        pds.setCharset(charset);

        try {
            if (initialPoolSize != null){
                pds.setInitialPoolSize(initialPoolSize);
            }
            if (minPoolSize != null){
                pds.setMinPoolSize(minPoolSize);
            }
            if (maxPoolSize != null){
                pds.setMaxPoolSize(maxPoolSize);
            }

            pds.fill();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }

    }

    @Override
    public void close() {
        initialized = false;
        try {
            pds.close();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public <RT> RT execute(RDFConnectionCallback<RT> operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
                try{
                    RT retVal = operation.doInConnection(connection);
                    tx.commit();
                    return retVal;
                }catch(IOException io){
                    tx.rollback();
                    throw io;
                }
            }finally{
                connection.close();
            }
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, UID context, OutputStream out) {
        RDFWriter writer = WriterUtils.createWriter(format, out, ns2prefix);
        RDFConnection conn = openConnection();
        try{
            CloseableIterator<STMT> stmts = conn.findStatements(null, null, null, context, false);
            try{
                writer.begin();
                while (stmts.hasNext()){
                    writer.handle(stmts.next());
                }
                writer.end();
            }finally{
                stmts.close();
            }

        }finally{
            conn.close();
        }
    }

    @Override
    public void export(Format format, UID context, OutputStream out) {
        export(format, Namespaces.DEFAULT, context, out);
    }

    public VirtuosoRepositoryConnection openConnection() {
        try {
            java.sql.Connection connection = pds.getConnection();
            return new VirtuosoRepositoryConnection(idSequence, converter, prefetchSize, defGraph, allowedGraphs, connection);
        } catch (SQLException e) {
            logger.error("Connection to " + host + " FAILED.");
            throw new RepositoryException(e);
        }
    }

    public File getDataDir() {
        return this.dataDir;
    }

    public int getFetchSize() {
        return this.prefetchSize;
    }

    public void initialize() {
        if (!initialized) {
            if (dataDir != null){
                idSequence = new FileIdSequence(new File(dataDir, "lastLocalId"));
            }else{
                idSequence = new MemoryIdSequence();
            }

            VirtuosoRepositoryConnection connection = openConnection();
            RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
            try {
                if (sources != null) {
                    for (RDFSource source : sources) {
                        if (source.getResource() != null){
                            logger.info("loading " + source.getResource());
                        }
                        UID context = new UID(source.getContext());
                        InputStream is = source.openStream();
                        try{
                            connection.load(source.getFormat(), is, context, false);
                        }finally{
                            is.close();
                        }
                    }
                }
                tx.commit();
            } catch(Exception e){
                tx.rollback();
                throw new RepositoryException(e);
            } finally {
                connection.close();
            }
            initialized = true;
        }
    }

    @Override
    public void load(Format format, InputStream is, UID context, boolean replace) {
        VirtuosoRepositoryConnection connection = openConnection();
        try{
            try{
                connection.load(format, is, context, replace);
            }finally{
                is.close();
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }finally{
            connection.close();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = Assert.notNull(dataDir,"dataDir");
    }

    public void setFetchSize(int sz) {
        this.prefetchSize = sz;
    }

    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }

    public void setAllowedGraphs(Collection<UID> allowedGraphs) {
        this.allowedGraphs = Assert.notNull(allowedGraphs,"allowedGraphs");
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }


}
