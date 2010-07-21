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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.types.EConstructor;
import com.mysema.query.types.Expr;
import com.mysema.query.types.path.PNumber;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.Session;

/**
 * RDBConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBConnection implements RDFConnection{
    
    private static final int ADD_BATCH = 50;
   
    public static final PNumber<Integer> one = new PNumber<Integer>(Integer.class,"1");
    
    public static final QSymbol sub = new QSymbol("subject");
    
    public static final QSymbol pre = new QSymbol("predicate");
    
    public static final QSymbol obj = new QSymbol("object");
    
    public static final QSymbol con = new QSymbol("context");
    
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
        List<Integer> persisted = context.createQuery()
            .from(language)
            .where(language.id.in(ids))
            .list(language.id);
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
    
    private void addNode(Long nodeId, NODE node) {
        SQLMergeClause merge = context.createMerge(symbol);
        merge.keys(symbol.id);
        merge.set(symbol.id, nodeId);
        merge.set(symbol.resource, node.isResource());
        merge.set(symbol.lexical, node.getValue());
        if (node.isLiteral()){
            LIT literal = node.asLiteral();
            merge.set(symbol.datatype, getId(literal.getDatatype()));    
            merge.set(symbol.lang, getLangId(literal.getLang()));
            if (Constants.integerTypes.contains(literal.getDatatype())){
                merge.set(symbol.integer, Long.valueOf(literal.getValue()));
            }
            if (Constants.decimalTypes.contains(literal.getDatatype())){
                merge.set(symbol.floating, Double.valueOf(literal.getValue()));
            }
            if (Constants.dateTypes.contains(literal.getDatatype())){
                merge.set(symbol.datetime, context.toDate(literal));                
            }
            if (Constants.dateTimeTypes.contains(literal.getDatatype())){
                merge.set(symbol.datetime, context.toTimestamp(literal));
            }
        }
        merge.execute();
    }
    
    private void addNodes(List<Long> ids, List<NODE> nodes){
        List<Long> persisted = context.createQuery()
            .from(symbol)
            .where(symbol.id.in(ids))
            .list(symbol.id);
        for (int i = 0; i < ids.size(); i++){
            Long id = ids.get(i);
            if (!persisted.contains(id)){
                addNode(id, nodes.get(i));
            }
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

    private void addStatement(STMT stmt) {
        SQLQuery query = context.createQuery();
        query.from(statement);
        if (stmt.getContext() != null){
            query.where(statement.model.eq(getId(stmt.getContext())));    
        }else{
            query.where(statement.model.isNull());
        }        
        query.where(statement.subject.eq(getId(stmt.getSubject())));
        query.where(statement.predicate.eq(getId(stmt.getPredicate())));
        query.where(statement.object.eq(getId(stmt.getObject())));
        if (query.count() > 0l){
            return;
        }
        
        // TODO : fix merge clause behaviour in Querydsl SQL
//        SQLMergeClause merge = context.createMerge(statement);
//        merge.keys(statement.model, statement.subject, statement.predicate, statement.object);
        SQLInsertClause insert = context.createInsert(statement);
        if (stmt.getContext() != null){
            insert.set(statement.model, getId(stmt.getContext()));    
        }else{
            insert.set(statement.model, null);
        }        
        insert.set(statement.subject, getId(stmt.getSubject()));
        insert.set(statement.predicate, getId(stmt.getPredicate()));
        insert.set(statement.object, getId(stmt.getObject()));
        insert.execute();
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
        return new BID("_"+UUID.randomUUID());
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
    
    public List<STMT> find(ID subject, UID predicate, NODE object, UID model, boolean includeInferred) {
        return IteratorAdapter.asList(findStatements(subject, predicate, object, model, includeInferred));
    }

    @Override
    @SuppressWarnings("serial")
    public CloseableIterator<STMT> findStatements(
            final ID subject, 
            final UID predicate, 
            final NODE object, 
            final UID model, boolean includeInferred) {
        SQLQuery query = this.context.createQuery();
        query.from(statement);
        List<Expr<?>> exprs = new ArrayList<Expr<?>>();
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
        
        // add dummy projection if none is specified
        if (exprs.isEmpty()){
            exprs.add(one);
        }
        
        EConstructor<STMT> stmt = new EConstructor<STMT>(STMT.class, new Class[0],exprs.toArray(new Expr[exprs.size()])){
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
            if (lex.startsWith("_")){
                return new BID(lex);
            }else{
                return new UID(lex);
            }
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
    
    private void removeStatement(STMT stmt) {
        SQLDeleteClause delete = context.createDelete(statement);
        if (stmt.getContext() == null){
            delete.where(statement.model.isNull());
        }else{
            delete.where(statement.model.eq(getId(stmt.getContext())));
        }
        delete.where(statement.subject.eq(getId(stmt.getSubject())));
        delete.where(statement.predicate.eq(getId(stmt.getPredicate())));
        delete.where(statement.object.eq(getId(stmt.getObject())));
        delete.execute();
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
            removeStatement(stmt);            
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
        for (STMT stmt : addedStatements){
            addStatement(stmt);
        }
        
    }

}
