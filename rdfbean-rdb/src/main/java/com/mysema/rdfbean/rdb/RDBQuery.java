/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.SearchResults;
import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.SQLCommonQuery;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.*;
import com.mysema.query.types.custom.CBoolean;
import com.mysema.query.types.custom.CSimple;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.ENumberConst;
import com.mysema.query.types.expr.OBoolean;
import com.mysema.query.types.expr.OSimple;
import com.mysema.query.types.path.PDate;
import com.mysema.query.types.path.PDateTime;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PTime;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;

/**
 * RDBQuery is a BeanQuery implementation for the RDB module
 * 
 * @author tiwe
 *
 */
public class RDBQuery extends ProjectableQuery<RDBQuery> implements BeanQuery{
    
    private final RDBContext context;
    
    private final Configuration configuration;
    
    private final Session session;
    
    private boolean optional = false;
    
    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();
    
    private final Set<Path<?>> inversePaths = new HashSet<Path<?>>();
    
    // $var rdf:type ?
    private Map<Path<?>,QStatement> variables = new HashMap<Path<?>,QStatement>();
    
    // ? ? $prop
    private Map<Path<?>,QStatement> properties = new HashMap<Path<?>,QStatement>();
    
    private Map<Path<?>,Expr<?>> symbols = new HashMap<Path<?>,Expr<?>>();
    
    public RDBQuery(RDBContext context, Session session) {
        super(new QueryMixin<RDBQuery>());
        queryMixin.setSelf(this);
        this.context = context;
        this.configuration = session.getConfiguration();
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    private <S,T> Expr<T> cast(Path<S> path, Class<T> type) {
        if (path.getType().equals(type)){
            return (Expr<T>)path.asExpr();
        }else{
            return new PSimple<T>(type, path.getMetadata());
        }
    }

    @Override
    public long count() {
        SQLQuery query = context.createQuery();
        populate(query);
        return query.count();
    }

    // simplify
    
    public SQLQuery createQuery(Expr<?>... args){
        SQLQuery query = context.createQuery();
        populate(query, args);
        return query;
    }
    
    @Override
    public BeanQuery from(EntityPath<?>... o) {
        queryMixin.from(o);
        return this;
    }
    
    private Long getId(NODE node) {
        return context.getNodeId(node);
    }

    private QStatement getProperty(SQLCommonQuery<?> query, Path<?> parent, Path<?> target){
        if (!properties.containsKey(target)){
            MappedClass mc = configuration.getMappedClass(parent.getType());
            MappedPath mp = mc.getMappedPath(target.getMetadata().getExpression().toString());
            // id property
            if (mp.getPredicatePath().isEmpty()){
                // local
                if (mp.getMappedProperty().getType().equals(String.class)){
                    QStatement statement = new QStatement(target.toString().replace('.', '_'));
                    properties.put(target, statement);
                    BooleanBuilder joinCondition = new BooleanBuilder();
                    if (variables.containsKey(parent)){
                        joinCondition.and(variables.get(parent).subject.eq(statement.subject));                            
                    }else if (properties.containsKey(parent)){
                        joinCondition.and(properties.get(parent).object.eq(statement.subject));
                    }else{
                        throw new IllegalStateException();
                    }                    
                    joinCondition.and(statement.predicate.eq(getId(CORE.localId)));
                    if (inOptionalPath()){
                        query.leftJoin(statement).on(joinCondition);
                    }else{
                        query.innerJoin(statement).on(joinCondition);    
                    }                            
                    return statement;
                    
                // resource    
                }else{
                    throw new IllegalStateException();
                }
                
            // other property 
            }else{
                QStatement statement = new QStatement(target.toString().replace('.', '_'));
                properties.put(target, statement);
                BooleanBuilder joinCondition = new BooleanBuilder();
                PNumber<Long> propSymbol = mp.isInverse(0) ? statement.object : statement.subject;
                if (mp.isInverse(0)){
                    inversePaths.add(target);
                }
                if (variables.containsKey(parent)){
                    // property of variable            
                    joinCondition.and(variables.get(parent).subject.eq(propSymbol));                            
                }else if (properties.containsKey(parent)){
                    joinCondition.and(properties.get(parent).object.eq(propSymbol));
                }else{
                    throw new IllegalStateException();
                }
                
                // TODO : support longer paths
                joinCondition.and(statement.predicate.eq(getId(mp.getPredicatePath().get(0).getUID())));
                if (inOptionalPath()){
                    query.leftJoin(statement).on(joinCondition);
                }else{
                    query.innerJoin(statement).on(joinCondition);    
                }                            
                return statement;    
            }                
        }else{
            return properties.get(target);
        }        
    }
    
    @SuppressWarnings("unchecked")
    private Expr<?> getSymbol(SQLCommonQuery<?> query, QStatement stmt, Path<?> path) {
        if (!symbols.containsKey(path)){
            QSymbol symbol = new QSymbol(stmt + "_symbol_");
            ForeignKey<QSymbol> fk;
            if (path.getMetadata().isRoot() || inversePaths.contains(path)){
                fk = stmt.subjectFk;
            }else{
                fk = stmt.objectFk;    
            }            
            if (inOptionalPath()){
                query.leftJoin(fk, symbol);   
            }else{
                query.innerJoin(fk, symbol);    
            }            
            Expr<?> expr;
            if (Constants.decimalClasses.contains(path.getType())){
                expr = cast(symbol.floating,path.getType());
            }else if (Number.class.isAssignableFrom(path.getType())){
                expr = cast(symbol.integer, path.getType());
            }else if (path.getType().equals(java.util.Date.class)){    
                expr = symbol.datetime;
            }else if (Constants.dateClasses.contains(path.getType())){
                expr = new PDate(path.getType(), symbol.datetime.getMetadata()); 
            }else if (Constants.dateTimeClasses.contains(path.getType())){    
                expr = new PDateTime(path.getType(), symbol.datetime.getMetadata());
            }else if (Constants.timeClasses.contains(path.getType())){
                expr = new PTime(path.getType(), symbol.datetime.getMetadata());
            }else{
                expr = symbol.lexical;
            }
            symbols.put(path, expr);
            return expr;
        }else{
            return symbols.get(path);
        }
    }
    
    @SuppressWarnings("unchecked")
    private QStatement getVariable(SQLCommonQuery<?> query, Path<?> target) {
        if (!variables.containsKey(target)){
            MappedClass mc = configuration.getMappedClass(target.getType());            
            QStatement statement = new QStatement(target.toString());
            variables.put(target, statement);
            MappedProperty<?> idProperty = mc.getIdProperty();
            if (idProperty != null && ID.class.isAssignableFrom(idProperty.getType())){
                PSimple<?> idPath = new PSimple(idProperty.getType(), target, idProperty.getName());
                variables.put(idPath, statement);
            }
            query.from(statement);            
            query.where(statement.predicate.eq(getId(RDF.type)));
            Long type = getId(mc.getUID());
            Collection<Long> subtypes = context.getOntology().getSubtypes(type);
            query.where(statement.object.in(subtypes));
            return statement;            
        }else{
            return variables.get(target);    
        }        
    }

    private boolean inOptionalPath(){
        return optional 
            || operatorStack.contains(Ops.IS_NULL) 
            || (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    private boolean isEntityType(Class<?> cl){
        // TODO : replace with proper check
        return ID.class.isAssignableFrom(cl) || cl.getAnnotation(ClassMapping.class) != null;
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expr<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.iterate(populate(query, args));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        return (CloseableIterator<RT>) query.iterate(populate(query, arg)[0]);
    }
        
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.list(populate(query, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> List<RT> list(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        return (List<RT>) query.list(populate(query, arg)[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        return (SearchResults<RT>) query.listResults(populate(query, arg)[0]);
    }

    private boolean needsSymbolResolving(Operation<?> op) {
        return (!Ops.equalsOps.contains(op.getOperator()) 
                && !Ops.notEqualsOps.contains(op.getOperator())
                && op.getOperator() != Ops.IN);
    }

    private Expr<?>[] populate(SQLCommonQuery<?> query, Expr<?>... projection){
        return populate(query, queryMixin.getMetadata(), projection);
    }
    
    @SuppressWarnings("unchecked")
    private Expr<?>[] populate(SQLCommonQuery<?> query, QueryMetadata metadata, Expr<?>... proj){
        Map<Path<?>,QStatement> v = null;
        Map<Path<?>,QStatement> p = null;
        Map<Path<?>,Expr<?>> s = null;
        
        // scope subquery data
        if (query instanceof SQLSubQuery){
            v = variables;
            p = properties;
            s = symbols;
            variables = new HashMap<Path<?>,QStatement>(variables);
            properties = new HashMap<Path<?>,QStatement>(properties);
            symbols = new HashMap<Path<?>,Expr<?>>(symbols);
        }
        
        // from
        for (JoinExpression join : metadata.getJoins()){
            Expr<?> target = join.getTarget();
            getVariable(query,(Path<?>) target);
        }
        
        // where
        if (metadata.getWhere() != null){
            query.where(transform(query, metadata.getWhere()));
        }
        
        // group by
        for (Expr<?> expr : metadata.getGroupBy()){
            query.groupBy(transform(query, expr, false));
        }
        
        // having
        if (metadata.getHaving() != null){
            query.having(transform(query, metadata.getHaving()));
        }
                
        // order
        optional = true;
        for (OrderSpecifier<?> order : metadata.getOrderBy()){            
            query.orderBy(new OrderSpecifier(order.getOrder(), transform(query,order.getTarget(),true)));            
        }
        optional = false;
        
        // paging
        if (metadata.getModifiers() != null){
            query.restrict(metadata.getModifiers());
        }        

        // projection        
        optional = true;
        Expr<?>[] projection = projection(query, proj);
        optional = false;
        
        // reset original data
        if (query instanceof SQLSubQuery){
            variables = v;
            properties = p;
            symbols = s;
        }
        
        return projection;
    }
    
    private Expr<?>[] projection(SQLCommonQuery<?> query, Expr<?>[] exprs) {
        Expr<?>[] rv = new Expr<?>[exprs.length];
        for (int i = 0; i < rv.length; i++){
            rv[i] = projection(query, exprs[i]);
        }
        return rv;
    }

    @SuppressWarnings({ "unchecked", "serial" })
    private <T> Expr<T> projection(SQLCommonQuery<?> query, final Expr<T> expr) {
        if (expr instanceof Path<?> && isEntityType(expr.getType())){
            Expr<?> source = (Expr<T>) transform(query, (Path<?>)expr, true);
            return new EConstructor<T>((Class)expr.getType(), new Class[0], source){
                @Override
                @Nullable
                public T newInstance(Object... args){
                    if (args[0] != null){
                        String id = args[0].toString();
                        return session.get(expr.getType(), context.getID(id, !id.startsWith("_")));    
                    }else{
                        return null;
                    }
                    
                }
            };
        }else{
            return (Expr<T>) transform(query, expr, true);
        }
    }

    @Override
    public String toString(){
        SQLQuery query = context.createQuery();
        populate(query);
        return query.toString();
    }
    
    private Expr<?> transform(SQLCommonQuery<?> query, Constant<?> constant, boolean realType){
        if (NODE.class.isAssignableFrom(constant.getConstant().getClass())){
            NODE node = (NODE)constant.getConstant();
            return ENumberConst.create(getId(node));
        }else if (!realType){    
            if (isEntityType(constant.getConstant().getClass())){
                ID id = session.getId(constant.getConstant());
                return ENumberConst.create(getId(id));
            }else{
                return ENumberConst.create(context.getId(constant.getConstant()));    
            }                        
        }else{
            return constant.asExpr();
        }
    }
    
    private Expr<?> transform(SQLCommonQuery<?> query, Custom<?> custom) {
        if (custom.getType().equals(Boolean.class)){
            return CBoolean.create(custom.getTemplate(), transform(query, custom.getArgs(), true));
        }else{
            return CSimple.create(custom.getType(), custom.getTemplate(), transform(query, custom.getArgs(), true));    
        }               
    }

    private EBoolean transform(SQLCommonQuery<?> query,EBoolean expr){
        return (EBoolean) transform(query, expr, false);
    }

    /**
     * main transformation method
     */
    @SuppressWarnings("unchecked")
    private Expr<?> transform(SQLCommonQuery<?> query, Expr<?> expr, boolean realType) {
        if (expr instanceof Path<?>){
            return transform(query, (Path<?>)expr, realType);
        }else if (expr instanceof Operation<?>){
            return transform(query, (Operation<?>)expr, realType);
        }else if (expr instanceof Constant){
            return transform(query, (Constant<?>)expr, realType);
        }else if (expr instanceof Custom<?>){
            return transform(query, (Custom<?>)expr);                 
        }else if (expr instanceof SubQuery){    
            return transform(query, (SubQuery<?>)expr, realType);
        }else if (expr instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)expr;
            return transform(query, bb.getValue(), realType);
        }else{
            return expr;    
        }
    }

    private Expr<?>[] transform(SQLCommonQuery<?> query, List<Expr<?>> exprs, boolean realType) {
        Expr<?>[] rv = new Expr[exprs.size()];
        for (int i = 0; i < rv.length; i++){
            rv[i] = transform(query, exprs.get(i), realType);
        }
        return rv;
    }
    
    @SuppressWarnings("unchecked")
    private Expr<?> transform(SQLCommonQuery<?> query, Operation<?> operation, boolean realType){
        Operator operator = operation.getOperator();
        boolean rt = needsSymbolResolving(operation);
        operatorStack.push(operator);        
        Expr<?>[] args = transform(query, operation.getArgs(), rt);
        if (operator == Ops.IN && !rt){
            operator = Ops.EQ_OBJECT;
            args = new Expr[]{args[1],args[0]};
        }
        operatorStack.pop();
        if (operation.getType().equals(Boolean.class)){
            return OBoolean.create(operator, args);
        }else{
            return OSimple.create(operation.getType(), operator, args);    
        }        
    }
    
    private Expr<?> transform(SQLCommonQuery<?> query, Path<?> path, boolean realType){        
        PathType pathType = path.getMetadata().getPathType();
        if (path.getMetadata().getParent() != null){
            transform(query,path.getMetadata().getParent().asExpr(), false);
        }        
        if (pathType == PathType.VARIABLE || variables.containsKey(path)){
            QStatement stmt = getVariable(query,path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return stmt.subject;
            }
            
        }else if (pathType == PathType.PROPERTY){            
            QStatement stmt = getProperty(query,path.getMetadata().getParent(), path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return inversePaths.contains(path) ? stmt.subject : stmt.object;
            }
        }else if (pathType == PathType.DELEGATE){       
            return transform(query, path.getMetadata().getExpression(), realType);
            
        }else{
            throw new IllegalArgumentException("Unsupported path type " + pathType);
        }
    }

    private Expr<?> transform(SQLCommonQuery<?> query, SubQuery<?> expr, boolean realType) {
        SubQuery<?> q = (SubQuery<?>)expr;
        List<? extends Expr<?>> p = q.getMetadata().getProjection();
        SQLSubQuery sq = new SQLSubQuery();
        Expr<?>[] projection = populate(sq, q.getMetadata(), p.toArray(new Expr[p .size()]));
        return sq.list(projection);            
    }
    
    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.uniqueResult(populate(query, args));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <RT> RT uniqueResult(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        return (RT) query.uniqueResult(populate(query, arg)[0]);
    }
    
}
