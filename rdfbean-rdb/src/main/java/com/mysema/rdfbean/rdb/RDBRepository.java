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
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.sql.DataSource;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.mysema.commons.lang.Assert;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * RDBRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class RDBRepository implements Repository{
    
    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();
    
    private final IdFactory idFactory = new MD5IdFactory();
    
    private final BidiMap<NODE,Long> nodeCache = new DualHashBidiMap<NODE,Long>();
    
    private final BidiMap<Locale,Integer> langCache = new DualHashBidiMap<Locale,Integer>();
    
    private final Configuration configuration; 
    
    private final RDBOntology ontology;
    
    private final DataSource dataSource;
    
    private final SQLTemplates templates;
    
    private final IdSequence idSequence;
    
    public RDBRepository(
            Configuration configuration,
            DataSource dataSource, 
            SQLTemplates templates, 
            IdSequence idSequence) {
        this.configuration = Assert.notNull(configuration,"configuration");
        this.ontology = new RDBOntology(idFactory,configuration);
        this.dataSource = Assert.notNull(dataSource,"dataSource");
        this.templates = Assert.notNull(templates,"templates");
        this.idSequence = Assert.notNull(idSequence, "idSequence");
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void execute(Operation operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                RDFBeanTransaction tx = connection.beginTransaction(false, 0, 
                        Connection.TRANSACTION_READ_COMMITTED);
                try{
                    operation.execute(connection);    
                    tx.commit();
                }catch(IOException io){
                    tx.rollback();
                }                
            }finally{
                connection.close();
            }    
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }

    @Override
    public void export(Format format, OutputStream os) {
        // TODO Auto-generated method stub        
    }

    @Override
    public void initialize() {        
        try {
            initSchema();
            initTables();
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @SuppressWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    private void initSchema() throws IOException, SQLException {
        Connection conn = dataSource.getConnection();
        SQLQuery query = new SQLQueryImpl(conn, templates).from(QLanguage.language);        
        try{
            query.count();
        } catch (Exception e) {
            // TODO make the schema source customizable
            InputStream is = getClass().getResourceAsStream("/h2.sql");
            String schema = IOUtils.toString(is, "UTF-8");
            for (String clause : StringUtils.split(schema, ";")){
                Statement stmt2 = conn.createStatement();
                try{
                    stmt2.execute(clause);    
                }finally{
                    stmt2.close();    
                }
            }
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
                nodes.add(new LIT(str.toUpperCase()));   
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
