/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.rdfbean.rdb.QLanguage.language;
import static com.mysema.rdfbean.rdb.QStatement.statement;
import static com.mysema.rdfbean.rdb.QSymbol.symbol;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.dml.DeleteClause;
import com.mysema.query.dml.StoreClause;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.ConstructorExpression;
import com.mysema.query.types.template.NumberTemplate;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.Session;

/**
 * RDBConnection is the RDFConnection implementation for the RDB module
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBConnection implements RDFConnection{
    
    private static final int ADD_BATCH = 200;
    
    private static final Locale DEFAULT_LOCALE = new Locale("en");
   
    private static final Timestamp DEFAULT_TIMESTAMP = new Timestamp(0);
    
    private static final int DELETE_BATCH = 200;
    
    public static final Expression<Integer> one = NumberTemplate.create(Integer.class,"1");
    
    public static final QSymbol con = new QSymbol("context");
    
    public static final QSymbol obj = new QSymbol("object");
    
    public static final QSymbol pre = new QSymbol("predicate");
    
    public static final QSymbol sub = new QSymbol("subject");
        
    private final RDBContext context;
    
    private final Transformer<Long,NODE> nodeTransformer = new Transformer<Long,NODE>(){
        @Override
        public NODE transform(Long id) {
            SQLQuery query = context.createQuery();
            query.from(symbol);
            query.where(symbol.id.eq(id));
            Object[] result = query.uniqueResult(symbol.resource, symbol.lexical, symbol.datatype, symbol.lang);
            if (result != null){
                return getNode((Boolean)result[0], (String)result[1], (Long)result[2], (Integer)result[3]);
            }else{
                throw new IllegalArgumentException("Found no node for id " + id);
            }
        }        
    };
    
    public RDBConnection(RDBContext context) {
        this.context = context;
    }
    
    private void addLocale(Integer id, Locale locale ){
        SQLMergeClause merge = context.createMerge(language);
        merge.keys(language.id);
        merge.set(language.id, id);
        merge.set(language.text, LocaleUtil.toLang(locale));
        merge.execute();
    }

    private void addLocales(List<Integer> ids, List<Locale> locales){
        Set<Integer> persisted = new HashSet<Integer>(context.createQuery()
            .from(language)
            .where(language.id.in(ids))
            .list(language.id));
        for (int i = 0; i < ids.size(); i++){
            Integer id = ids.get(i);
            if (!persisted.contains(id)){
                addLocale(id, locales.get(i));
            }       
        }
        ids.clear();
        locales.clear();
    }
    
    public void addLocales(Set<Locale> l, @Nullable BidiMap<Locale,Integer> cache){
        List<Integer> ids = new ArrayList<Integer>(ADD_BATCH);
        List<Locale> locales = new ArrayList<Locale>(ADD_BATCH);
        for (Locale locale : l){
            Integer id = context.getLangId(locale);
            ids.add(id);
            locales.add(locale);
            if (cache != null){
                cache.put(locale, id);
            }
            if (ids.size() == ADD_BATCH){
                addLocales(ids, locales);
            }
        }
        if (!ids.isEmpty()){
            addLocales(ids, locales);
        }
    }
    
    private void addNodes(List<Long> ids, List<NODE> nodes){
        Set<Long> persisted = new HashSet<Long>(context.createQuery()
            .from(symbol)
            .where(symbol.id.in(ids))
            .list(symbol.id));
        
        if (persisted.size() < ids.size()){
            SQLMergeClause merge = context.createMerge(symbol);
            for (int i = 0; i < ids.size(); i++){
                Long id = ids.get(i);
                if (!persisted.contains(id)){
                    populate(merge, symbol, id, nodes.get(i)).addBatch();                       
                }
            }
            merge.execute();
        }        
        ids.clear();
        nodes.clear();
    }
    
    public void addNodes(Set<NODE> n, @Nullable BidiMap<NODE,Long> cache) {
        List<Long> ids = new ArrayList<Long>(ADD_BATCH);
        List<NODE> nodes = new ArrayList<NODE>(ADD_BATCH);
        for (NODE node : n){
            Long nodeId = getId(node);
            ids.add(nodeId);
            nodes.add(node);
            if (cache != null){
                cache.put(node, nodeId);
            }
            if (ids.size() == ADD_BATCH){
                addNodes(ids, nodes);
            }
        }
        if (!ids.isEmpty()){
            addNodes(ids, nodes);
        }
    }
    
    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        return context.beginTransaction(readOnly, txTimeout, isolationLevel);
    }

    @Override
    public void clear() {
        context.clear();        
    }

    @Override
    public void close() throws IOException {
        context.close();
    }
    
    @Override
    public BID createBNode() {
        return new BID(UUID.randomUUID().toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.QUERYDSL)){
            return (Q)new RDBQuery(context,session);            
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);
        }
    }

    public void deleteFromContext(UID model) {
        SQLDeleteClause delete = context.createDelete(statement);
        delete.where(statement.model.eq(getId(model)));
        delete.execute();
    }
       
    public List<STMT> find(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID model, boolean includeInferred) {
        return IteratorAdapter.asList(findStatements(subject, predicate, object, model, includeInferred));
    }

    @Override
    @SuppressWarnings("serial")
    public CloseableIterator<STMT> findStatements(
            @Nullable final ID subject, 
            @Nullable final UID predicate, 
            @Nullable final NODE object, 
            @Nullable final UID model, boolean includeInferred) {
        SQLQuery query = this.context.createQuery();
        query.from(statement);
        List<Expression<?>> exprs = new ArrayList<Expression<?>>();
        if (subject != null){
            query.where(statement.subject.eq(getId(subject)));
        }else{
            query.innerJoin(statement.subjectFk, sub);
            exprs.add(sub.lexical);
        }
        if (predicate != null){
            query.where(statement.predicate.eq(getId(predicate)));
        }else{
            exprs.add(statement.predicate);
        }
        if (object != null){
            if (RDF.type.equals(predicate) && includeInferred){
                Collection<Long> ids = context.getOntology().getSubtypes(getId(object));
                query.where(statement.object.in(ids));
            }else{
                query.where(statement.object.eq(getId(object)));    
            }            
        }else{
            query.innerJoin(statement.objectFk, obj);
            exprs.add(obj.resource);
            exprs.add(obj.lexical);
            exprs.add(obj.datatype);
            exprs.add(obj.lang);
        }
        if (model != null){
            query.where(statement.model.eq(getId(model)));
        }else{
            exprs.add(statement.model);
        }
        
        // return ordered result, if all triples are queried
        if (subject == null && predicate == null && object == null && model == null){
            query.orderBy(statement.model.asc());
            query.orderBy(statement.subject.asc());
            query.orderBy(statement.predicate.asc());
            query.orderBy(statement.object.asc());
        }
        
        // add dummy projection if none is specified
        if (exprs.isEmpty()){
            exprs.add(one);
        }
        
        Expression<STMT> stmt = new ConstructorExpression<STMT>(STMT.class, new Class[0],exprs.toArray(new Expression[exprs.size()])){
            @Override
            public STMT newInstance(Object... args) {
                ID s = subject;
                UID p = predicate;
                NODE o = object;
                UID m = model;
                int counter = 0;
                if (s == null){
                    s = getNode(true, (String)args[counter++], null, null).asResource();
                }
                if (p == null){
                    p = getNode((Long) args[counter++]).asURI();
                }                
                if (o == null){
                    o = getNode((Boolean)args[counter++], (String)args[counter++], (Long)args[counter++], (Integer)args[counter++]);
                }
                if (m == null && args[counter] != null && !args[counter].equals(Long.valueOf(0l))){
                    m = getNode((Long) args[counter]).asURI();
                    if (m.equals(RDB.nullContext)){
                        m = null;
                    }
                }
                return new STMT(s, p, o, m);
            }
      
        };        
        return query.iterate(stmt);
        

    }

    private Long getId(NODE node) {
        return context.getNodeId(node);
    }

    @Nullable
    private Integer getLangId(@Nullable Locale lang) {
        if (lang == null){
            return null;
        }else{
            return context.getLangId(lang);    
        }
    }

    private Locale getLocale(int id){
        return context.getLang(id);
    }
    
    @Override
    public long getNextLocalId() {
        return context.getNextLocalId();
    }

    private NODE getNode(boolean res, String lex, Long datatype, Integer lang){
        if (res){
            return context.getID(lex);
        }else{
            if (lang != null && !lang.equals(Integer.valueOf(0))){
                return new LIT(lex, getLocale(lang));
            }else if (datatype != null && !datatype.equals(Long.valueOf(0l))){
                return new LIT(lex, getNode(datatype).asURI());
            }else{
                return new LIT(lex);
            }
        }
    }
    
    private NODE getNode(long id) {
        return context.getNode(id, nodeTransformer);
    }
    
    private <C extends StoreClause<C>> C populate(C clause, QStatement statement, STMT stmt){
        Long c = stmt.getContext() != null ? getId(stmt.getContext()) : getId(RDB.nullContext);
        Long s = getId(stmt.getSubject());
        Long p = getId(stmt.getPredicate());
        Long o = getId(stmt.getObject());
        
        clause.set(statement.model, c);
        clause.set(statement.subject, s);
        clause.set(statement.predicate, p);
        clause.set(statement.object, o);
        return clause;
    }
    
    private <C extends DeleteClause<C>> C populate(C clause, QStatement statement, STMT stmt) {
        Long c = stmt.getContext() != null ? getId(stmt.getContext()) : getId(RDB.nullContext);
        Long s = getId(stmt.getSubject());
        Long p = getId(stmt.getPredicate());
        Long o = getId(stmt.getObject());
        
        clause.where(statement.model.eq(c));
        clause.where(statement.subject.eq(s));
        clause.where(statement.predicate.eq(p));
        clause.where(statement.object.eq(o));
        return clause;
    }

    private <C extends StoreClause<C>> C populate(C clause, QSymbol symbol, Long nodeId, NODE node){
        long datatypeId = getId(XSD.anyURI);
        int langId = getLangId(DEFAULT_LOCALE);
        long intVal = 0l;
        double floatVal = 0.0;
        Timestamp datetimeVal = DEFAULT_TIMESTAMP;
        
        clause.set(symbol.id, nodeId);
        clause.set(symbol.resource, node.isResource());
        clause.set(symbol.lexical, node.getValue());
        
        if (node.isLiteral()){
            LIT literal = node.asLiteral();
            datatypeId = getId(literal.getDatatype());
            if (literal.getLang() != null){
                langId = getLangId(literal.getLang());    
            }else if (Constants.integerTypes.contains(literal.getDatatype())){
                intVal = Long.valueOf(literal.getValue());
            }else if (Constants.decimalTypes.contains(literal.getDatatype())){
                floatVal = Double.valueOf(literal.getValue());
            }else if (Constants.dateTypes.contains(literal.getDatatype())){
                datetimeVal = new Timestamp(context.toDate(literal).getTime());     
            }else if (Constants.dateTimeTypes.contains(literal.getDatatype())){
                datetimeVal = context.toTimestamp(literal);
            }
        }
        
        clause.set(symbol.datatype, datatypeId);
        clause.set(symbol.lang, langId);
        clause.set(symbol.intval, intVal);
        clause.set(symbol.floatval, floatVal);
        clause.set(symbol.datetimeval, datetimeVal);       
        return clause;
    }
    

    @Override
    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        // remove
        Set<NODE> oldNodes = new HashSet<NODE>();
        for (STMT stmt : removedStatements){
            if (stmt.getContext() != null){
                oldNodes.add(stmt.getContext());
            }
            oldNodes.add(stmt.getSubject());
            oldNodes.add(stmt.getPredicate());
            oldNodes.add(stmt.getObject());   
        }
        
        if (!removedStatements.isEmpty()){
            Iterator<STMT> stmts = removedStatements.iterator();
            SQLDeleteClause delete = context.createDelete(statement);
            populate(delete, statement, stmts.next()).addBatch();
            int counter = 1;
            while (stmts.hasNext()){
                counter++;
                populate(delete, statement, stmts.next()).addBatch();
                if (counter == DELETE_BATCH){
                    delete.execute();
                    delete = context.createDelete(statement);
                    counter = 0;
                }                
            }
            if (counter > 0){
                delete.execute();    
            }    
        }
        
        // insert
        Set<NODE> newNodes = new HashSet<NODE>();
        for (STMT stmt : addedStatements){
            if (stmt.getContext() != null){
                newNodes.add(stmt.getContext());
            }
            newNodes.add(stmt.getSubject());
            newNodes.add(stmt.getPredicate());
            newNodes.add(stmt.getObject());           
            if (stmt.getObject().isLiteral()){
                LIT lit = stmt.getObject().asLiteral();
                if (lit.getDatatype() != null){
                    newNodes.add(lit.getDatatype());
                }
            }
        }
        
        // insert nodes
        newNodes.removeAll(oldNodes);
        newNodes.removeAll(context.getNodes());
        addNodes(newNodes, null);
        
        // insert stmts
        if (!addedStatements.isEmpty()){
            Iterator<STMT> stmts = addedStatements.iterator();
            SQLMergeClause merge = context.createMerge(statement);
            populate(merge, statement, stmts.next()).addBatch();
            int counter = 1;
            while (stmts.hasNext()){
                counter++;
                populate(merge, statement, stmts.next()).addBatch();
                if (counter == ADD_BATCH){
                    merge.execute();
                    merge = context.createMerge(statement);
                    counter = 0;
                }                
            }
            if (counter > 0){
                merge.execute();    
            }            
        }
        
    }


}
