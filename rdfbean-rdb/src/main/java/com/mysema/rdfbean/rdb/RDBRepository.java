/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.ddl.CreateTableClause;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.rdb.support.SesameDialect;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * RDBRepository is a Repository implementation for the RDB module
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class RDBRepository implements Repository{
    
    private static RDFFormat getRioFormat(Format format){
        switch(format){
            case N3: return RDFFormat.N3;
            case NTRIPLES: return RDFFormat.NTRIPLES;
            case RDFA: return RDFFormat.RDFA;
            case RDFXML: return RDFFormat.RDFXML;
            case TRIG: return RDFFormat.TRIG;
            case TURTLE: return RDFFormat.TURTLE;
        }
        throw new IllegalArgumentException("Unsupported format : " + format);
    }
        
    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();
    
    private final IdFactory idFactory = new MD5IdFactory();
    
    private final BidiMap<NODE,Long> nodeCache = new DualHashBidiMap<NODE,Long>();
    
    private final BidiMap<Locale,Integer> langCache = new DualHashBidiMap<Locale,Integer>();
    
    private final Configuration configuration; 
    
    private final RDBOntology ontology;
    
    private final DataSource dataSource;
    
    private final SQLTemplates templates;
    
    private final IdSequence idSequence;
    
    private final RDFSource[] sources;
    
    public RDBRepository(
            Configuration configuration,
            DataSource dataSource, 
            SQLTemplates templates, 
            IdSequence idSequence,
            RDFSource... sources) {
        this.configuration = Assert.notNull(configuration,"configuration");
        this.ontology = new RDBOntology(idFactory,configuration);
        this.dataSource = Assert.notNull(dataSource,"dataSource");
        this.templates = Assert.notNull(templates,"templates");
        this.idSequence = Assert.notNull(idSequence, "idSequence");
        this.sources = Assert.notNull(sources,"sources");
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public <RT> RT execute(Operation<RT> operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                RDFBeanTransaction tx = connection.beginTransaction(false, 0, 
                        Connection.TRANSACTION_READ_COMMITTED);
                try{
                    RT retVal = operation.execute(connection);    
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
    public void export(Format format, OutputStream out) {        
        RDFFormat targetFormat = getRioFormat(format);
        RDFWriter writer = Rio.createWriter(targetFormat, out);
        try {
            RDFConnection conn = openConnection();
            try{
                writer.startRDF();
                CloseableIterator<STMT> stmts = conn.findStatements(null, null, null, null, false);
                ValueFactory valueFactory = new ValueFactoryImpl();
                SesameDialect dialect = new SesameDialect(valueFactory);
                try{
                    while (stmts.hasNext()){
                        STMT stmt = stmts.next();
                        Resource sub = dialect.getResource(stmt.getSubject());
                        URI pre = dialect.getURI(stmt.getPredicate());
                        Value obj = dialect.getNode(stmt.getObject());
                        writer.handleStatement(valueFactory.createStatement(sub, pre, obj));
                    }    
                }finally{
                    stmts.close();
                }                
                writer.endRDF();
            }finally{
                conn.close();
            }
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (IOException e){
            throw new RepositoryException(e.getMessage(), e);
        }
    }
    
    @Override
    public void load(Format format, InputStream is, @Nullable UID context, boolean replace){
        ValueFactory valueFactory = new ValueFactoryImpl();
        SesameDialect dialect = new SesameDialect(valueFactory);
        RDBConnection connection = openConnection();
        try{
            Set<STMT> stmts = new HashSet<STMT>();
            if (!replace && context != null){
                if (!connection.find(null, null, null, context, false).isEmpty()){
                    return;
                }
            }
            RDFParser parser = Rio.createParser(getRioFormat(format));
            parser.setRDFHandler(createHandler(context, dialect, stmts));
            parser.parse(is, context != null ? context.getValue() : null);
            if (context != null && replace){
                connection.deleteFromContext(context);    
            }            
            connection.update(Collections.<STMT>emptySet(), stmts);    
        } catch (RDFParseException e) {
            throw new RepositoryException(e);
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }finally{
            try {
                connection.close();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }        
    }

    @Override
    public void initialize() {        
        try {
            initSchema();
            initTables();
            
            if (sources.length > 0){
                RDBConnection connection = openConnection();
                try{
                    ValueFactory valueFactory = new ValueFactoryImpl();
                    SesameDialect dialect = new SesameDialect(valueFactory);
                    for (RDFSource source : sources){                        
                        Set<STMT> stmts = new HashSet<STMT>();
                        RDFFormat format = getRioFormat(source.getFormat());
                        RDFParser parser = Rio.createParser(format);
                        UID context = new UID(source.getContext());
                        parser.setRDFHandler(createHandler(context, dialect, stmts));
                        parser.parse(source.openStream(), source.getContext());
                        connection.deleteFromContext(context);
                        connection.update(Collections.<STMT>emptySet(), stmts);
                    }
                } catch (RDFParseException e) {
                    throw new RepositoryException(e);
                } catch (RDFHandlerException e) {
                    throw new RepositoryException(e);
                }finally{
                    connection.close();
                }
            }            
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }
    
    private RDFHandler createHandler(final UID context, final SesameDialect dialect, final Set<STMT> stmts) {
        return new RDFHandlerBase(){
            @Override
            public void handleStatement(Statement stmt) throws RDFHandlerException {
                ID sub = dialect.getID(stmt.getSubject());
                UID pre = dialect.getUID(stmt.getPredicate());
                NODE obj = dialect.getNODE(stmt.getObject());
                stmts.add(new STMT(sub, pre, obj, context));                                                              
            }                            
        };
    }

    @SuppressWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    private void initSchema() throws IOException, SQLException {
        Connection conn = dataSource.getConnection();                
        try{
            SQLQuery query = new SQLQueryImpl(conn, templates).from(QLanguage.language);
            query.count();
        } catch (Exception e) {
            // language
            new CreateTableClause(conn,templates,"language")
            .column("id", Integer.class).notNull()
            .column("text", String.class).size(256).notNull()
            .primaryKey("pk_language", "id")
            .execute();
            
            // symbol
            new CreateTableClause(conn,templates,"symbol")
            .column("id", Long.class).notNull()
            .column("resource", Boolean.class).notNull()
            .column("lexical", String.class).size(1024).notNull()
            .column("datatype", Long.class)
            .column("lang", Integer.class)
            .column("integer", Long.class)
            .column("floating", Double.class)
            .column("datetime", Timestamp.class)
            .primaryKey("pk_symbol", "id")
            .foreignKey("fk_lang", "lang").references("language", "id")
            .execute();
            
            // statement
             new CreateTableClause(conn,templates,"statement")
            .column("model", Long.class)
            .column("subject",Long.class).notNull()
            .column("predicate", Long.class).notNull()
            .column("object", Long.class).notNull()
            .foreignKey("fk_model", "model").references("symbol", "id")
            .foreignKey("fk_subject", "subject").references("symbol", "id")
            .foreignKey("fk_predicate", "predicate").references("symbol", "id")
            .foreignKey("fk_object", "object").references("symbol", "id")
            .index("spo", "subject","predicate","object")
            .index("ops", "object","predicate","subject")
            .index("m","model")
            .execute();
        }finally{
            conn.close();
        }
        
    }
    
    private void initTables() throws IOException {
        RDBConnection conn = openConnection();
        try{
            Set<NODE> nodes = new HashSet<NODE>();
            
            // ontology resources
            for (MappedClass mappedClass : configuration.getMappedClasses()){
                // class id
                nodes.add(mappedClass.getUID());
                
                // enum constants
                if (mappedClass.isEnum()){
                    for (Object e : mappedClass.getJavaClass().getEnumConstants()){
                        nodes.add(new UID(mappedClass.getUID().ns(), ((Enum<?>)e).name()));
                    }
                }
                
                // property predicates
                for (MappedPath path : mappedClass.getProperties()){
                    MappedProperty<?> property = path.getMappedProperty();
                    if (property.getKeyPredicate() != null){
                        nodes.add(property.getKeyPredicate());
                    }
                    if (property.getValuePredicate() != null){
                        nodes.add(property.getValuePredicate());
                    }
                    for (MappedPredicate predicate : path.getPredicatePath()){
                        nodes.add(predicate.getUID());
                    }
                }
            }
            
            // common resources
            nodes.add(CORE.localId);
            nodes.addAll(RDF.ALL);
            nodes.addAll(RDFS.ALL);
            nodes.addAll(XSD.ALL);
            nodes.addAll(OWL.ALL);   
            
            // common literals
            nodes.add(new LIT(""));
            nodes.add(new LIT("true",XSD.booleanType));
            nodes.add(new LIT("false",XSD.booleanType));
            
            // dates
            nodes.add(new LIT(converterRegistry.toString(new java.sql.Date(0)), XSD.date));
            nodes.add(new LIT(converterRegistry.toString(new java.util.Date(0)), XSD.dateTime));
            
            // letters
            for (char c = 'a'; c <= 'z'; c++){
                String str = String.valueOf(c);
                nodes.add(new LIT(str));
                nodes.add(new LIT(str.toUpperCase(Locale.ENGLISH)));   
            }            
            
            // numbers
            for (int i = -128; i < 128; i++){
                String str = String.valueOf(i);
                nodes.add(new LIT(str));
                nodes.add(new LIT(str, XSD.byteType));
                nodes.add(new LIT(str, XSD.shortType));
                nodes.add(new LIT(str, XSD.intType));                
                nodes.add(new LIT(str, XSD.longType));
                nodes.add(new LIT(str, XSD.integerType));
                nodes.add(new LIT(str+".0", XSD.floatType));
                nodes.add(new LIT(str+".0", XSD.doubleType));                
                nodes.add(new LIT(str+".0", XSD.decimalType));                
            }
            
            conn.addNodes(nodes, nodeCache);
                        
            // init languages
            Set<Locale> locales = new HashSet<Locale>(Arrays.asList(Locale.getAvailableLocales()));
            locales.add(new Locale("fi"));
            locales.add(new Locale("sv"));
            
            conn.addLocales(locales, langCache);
            
        }finally{
            conn.close();
        }                
    }

    @Override
    public RDBConnection openConnection() {
        try {
            Connection connection = dataSource.getConnection();
            RDBContext context = new RDBContext(
                    converterRegistry, 
                    ontology, 
                    idFactory, 
                    nodeCache, langCache, 
                    idSequence, 
                    connection, 
                    templates); 
            return new RDBConnection(context);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }        
    }

}
