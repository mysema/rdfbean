/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.ConstructorExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExtractorVisitor;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathType;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.Templates;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.expr.SimpleOperation;
import com.mysema.query.types.expr.StringOperation;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.TimePath;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.query.types.template.SimpleTemplate;
import com.mysema.query.types.template.StringTemplate;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;

/**
 * RDBQuery is a BeanQuery implementation for the RDB module
 *
 * @author tiwe
 *
 */
public class RDBBeanQuery extends ProjectableQuery<RDBBeanQuery> implements BeanQuery{

    private static final Templates TEMPLATES = new Templates(){
        {
            add(PathType.PROPERTY, "{0}_{1}");
            add(PathType.COLLECTION_ANY, "{0}");
            add(PathType.LISTVALUE_CONSTANT, "{0}_{1}");
            add(PathType.ARRAYVALUE_CONSTANT, "{0}_{1}");
            add(PathType.MAPVALUE_CONSTANT, "{0}_{1}");
        }};

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

    private Map<Path<?>,Expression<?>> symbols = new HashMap<Path<?>,Expression<?>>();

    public RDBBeanQuery(RDBContext context, Session session) {
        super(new QueryMixin<RDBBeanQuery>());
        queryMixin.setSelf(this);
        this.context = context;
        this.configuration = session.getConfiguration();
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    private <S,T> Expression<T> cast(Path<S> path, Class<T> type) {
        if (path.getType().equals(type)){
            return (Expression<T>)path;
        }else{
            return new SimplePath<T>(type, path.getMetadata());
        }
    }

    @Override
    public long count() {
        SQLQuery query = context.createQuery();
        populate(query);
        return query.count();
    }    

    @Override
    public boolean exists() {
        SQLQuery query = context.createQuery();
        populate(query);
        return query.exists();
    }

    public SQLQuery createQuery(Expression<?>... args){
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
                    String str = target.accept(ToStringVisitor.DEFAULT, TEMPLATES);
                    QStatement statement = new QStatement(str);
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
                return getProperty(query, parent, target, mp.getPredicatePath());
            }
        }else{
            return properties.get(target);
        }
    }

    private QStatement getProperty(SQLCommonQuery<?> query, Path<?> parent, Path<?> target, List<MappedPredicate> predicates) {
        String str = target.accept(ToStringVisitor.DEFAULT, TEMPLATES);
        QStatement statement = new QStatement(str);
        properties.put(target, statement);
        BooleanBuilder joinCondition = new BooleanBuilder();
        NumberPath<Long> propSymbol = predicates.get(0).inv() ? statement.object : statement.subject;
        if (predicates.get(0).inv()){
            inversePaths.add(target);
        }
        if (parent.getMetadata().getPathType() == PathType.COLLECTION_ANY){
            parent = parent.getMetadata().getParent();
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
        joinCondition.and(statement.predicate.eq(getId(predicates.get(0).getUID())));
        if (inOptionalPath()){
            query.leftJoin(statement).on(joinCondition);
        }else{
            query.innerJoin(statement).on(joinCondition);
        }
        return statement;
    }

    @SuppressWarnings("unchecked")
    private Expression<?> getSymbol(SQLCommonQuery<?> query, QStatement stmt, Path<?> path) {
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
            Expression<?> expr;
            if (Constants.decimalClasses.contains(path.getType())){
                expr = cast(symbol.floatval,path.getType());
            }else if (Number.class.isAssignableFrom(path.getType())){
                expr = cast(symbol.intval, path.getType());
            }else if (path.getType().equals(java.util.Date.class)){
                expr = symbol.datetimeval;
            }else if (Constants.dateClasses.contains(path.getType())){
                expr = new DatePath(path.getType(), symbol.datetimeval.getMetadata());
            }else if (Constants.dateTimeClasses.contains(path.getType())){
                expr = new DateTimePath(path.getType(), symbol.datetimeval.getMetadata());
            }else if (Constants.timeClasses.contains(path.getType())){
                expr = new TimePath(path.getType(), symbol.datetimeval.getMetadata());
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
            String str = target.accept(ToStringVisitor.DEFAULT, TEMPLATES);
            QStatement statement = new QStatement(str);
            variables.put(target, statement);
            MappedProperty<?> idProperty = mc.getIdProperty();
            if (idProperty != null && ID.class.isAssignableFrom(idProperty.getType())){
                SimplePath<?> idPath = new SimplePath(idProperty.getType(), target, idProperty.getName());
                variables.put(idPath, statement);
            }
            query.from(statement);
            query.where(statement.predicate.eq(getId(RDF.type)));
            if (mc.getUID() == null){
                throw new IllegalStateException("No uid for " + mc.getJavaClass().getName());
            }
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
        return ID.class.isAssignableFrom(cl) || configuration.isMapped(cl);
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expression<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.iterate(populate(query, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> CloseableIterator<RT> iterate(Expression<RT> arg) {
        SQLQuery query = context.createQuery();
        return (CloseableIterator<RT>) query.iterate(populate(query, arg)[0]);
    }

    @Override
    public List<Object[]> list(Expression<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.list(populate(query, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> List<RT> list(Expression<RT> arg) {
        SQLQuery query = context.createQuery();
        return (List<RT>) query.list(populate(query, arg)[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> SearchResults<RT> listResults(Expression<RT> arg) {
        SQLQuery query = context.createQuery();
        return (SearchResults<RT>) query.listResults(populate(query, arg)[0]);
    }

    private boolean needsSymbolResolving(Operation<?> op) {
        if (Ops.equalsOps.contains(op.getOperator())
         || Ops.notEqualsOps.contains(op.getOperator())
         || op.getOperator() == Ops.IN
         || op.getOperator() == Ops.ORDINAL){
            for (Expression<?> arg : op.getArgs()){
                if (!(arg instanceof Path<?>) && !(arg instanceof Constant<?>)){
                    return true;
                }
            }
            return false;
        }else{
            return true;
        }
    }

    private Expression<?>[] populate(SQLCommonQuery<?> query, Expression<?>... projection){
        return populate(query, queryMixin.getMetadata(), projection);
    }

    @SuppressWarnings("unchecked")
    private Expression<?>[] populate(SQLCommonQuery<?> query, QueryMetadata metadata, Expression<?>... proj){
        Map<Path<?>,QStatement> v = null;
        Map<Path<?>,QStatement> p = null;
        Map<Path<?>,Expression<?>> s = null;

        // scope subquery data
        if (query instanceof SQLSubQuery){
            v = variables;
            p = properties;
            s = symbols;
            variables = new HashMap<Path<?>,QStatement>(variables);
            properties = new HashMap<Path<?>,QStatement>(properties);
            symbols = new HashMap<Path<?>,Expression<?>>(symbols);
        }

        // from
        for (JoinExpression join : metadata.getJoins()){
            Expression<?> target = join.getTarget();
            getVariable(query,(Path<?>) target);
        }

        // where
        if (metadata.getWhere() != null){
            query.where(transform(query, metadata.getWhere()));
        }

        // group by
        for (Expression<?> expr : metadata.getGroupBy()){
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
        Expression<?>[] projection = projection(query, proj);
        optional = false;

        // reset original data
        if (query instanceof SQLSubQuery){
            variables = v;
            properties = p;
            symbols = s;
        }

        return projection;
    }

    private Expression<?>[] projection(SQLCommonQuery<?> query, Expression<?>[] exprs) {
        Expression<?>[] rv = new Expression<?>[exprs.length];
        for (int i = 0; i < rv.length; i++){
            rv[i] = projection(query, exprs[i]);
        }
        return rv;
    }

    @SuppressWarnings({ "unchecked", "serial" })
    private <T> Expression<T> projection(SQLCommonQuery<?> query, final Expression<T> expr) {
        if (expr instanceof Path<?> && isEntityType(expr.getType())){
            Expression<?> source = transform(query, (Path<?>)expr, true);
            return new ConstructorExpression<T>((Class)expr.getType(), new Class[0], source){
                @Override
                @Nullable
                public T newInstance(Object... args){
                    if (args[0] != null){
                        String id = args[0].toString();
                        return session.get(expr.getType(), context.getID(id));
                    }else{
                        return null;
                    }
                }
            };
        }else{
            return (Expression<T>) transform(query, expr, true);
        }
    }

    @Override
    public String toString(){
        SQLQuery query = context.createQuery();
        populate(query);
        return query.toString();
    }

    private Expression<?> transform(SQLCommonQuery<?> query, Constant<?> constant, boolean realType){
        if (NODE.class.isAssignableFrom(constant.getConstant().getClass())){
            NODE node = (NODE)constant.getConstant();
            return ConstantImpl.create(getId(node));
        }else if (!realType){
            if (isEntityType(constant.getConstant().getClass())){
                ID id = session.getId(constant.getConstant());
                return ConstantImpl.create(getId(id));
            }else if (Collection.class.isAssignableFrom(constant.getConstant().getClass())){
                List<Object> ids = new ArrayList<Object>();
                for (Object o : (Collection<?>)constant.getConstant()){
                    if (isEntityType(o.getClass())){
                        ids.add(getId(session.getId(o)));
                    }else{
                        ids.add(context.getId(o));
                    }
                }
                return new ConstantImpl<Object>(ids);
            }else{
                return ConstantImpl.create(context.getId(constant.getConstant()));
            }
        }else{
            return constant;
        }
    }

    private Expression<?> transform(SQLCommonQuery<?> query, TemplateExpression<?> custom) {
        if (custom.getType().equals(Boolean.class)){
            return BooleanTemplate.create(custom.getTemplate(), transform(query, custom.getArgs(), true));
        }else if (custom.getType().equals(String.class)){
            return StringTemplate.create(custom.getTemplate(), transform(query, custom.getArgs(), true));
        }else{
            return SimpleTemplate.create(custom.getType(), custom.getTemplate(), transform(query, custom.getArgs(), true));
        }
    }

    private Predicate transform(SQLCommonQuery<?> query, Predicate expr){
        return (Predicate) transform(query, expr, false);
    }

    /**
     * main transformation method
     */
    @SuppressWarnings("unchecked")
    private Expression<?> transform(SQLCommonQuery<?> query, Expression<?> e, boolean realType) {
        Expression<?> expr = e.accept(ExtractorVisitor.DEFAULT, null);
        if (expr instanceof Path<?>){
            return transform(query, (Path<?>)expr, realType);
        }else if (expr instanceof Operation<?>){
            return transform(query, (Operation<?>)expr, realType);
        }else if (expr instanceof Constant){
            return transform(query, (Constant<?>)expr, realType);
        }else if (expr instanceof TemplateExpression<?>){
            return transform(query, (TemplateExpression<?>)expr);
        }else if (expr instanceof SubQueryExpression){
            return transform(query, (SubQueryExpression<?>)expr, realType);
        }else{
            throw new IllegalArgumentException(e.toString());
        }
    }

    private Expression<?>[] transform(SQLCommonQuery<?> query, List<Expression<?>> exprs, boolean realType) {
        Expression<?>[] rv = new Expression[exprs.size()];
        for (int i = 0; i < rv.length; i++){
            rv[i] = transform(query, exprs.get(i), realType);
        }
        return rv;
    }

    @SuppressWarnings("unchecked")
    private Expression<?> transform(SQLCommonQuery<?> query, Operation<?> operation, boolean realType){
        Operator operator = operation.getOperator();
        boolean rt = needsSymbolResolving(operation);
        operatorStack.push(operator);
        Expression<?>[] args = transform(query, operation.getArgs(), rt);
        if (operator == Ops.IN && !rt && operation.getArg(1) instanceof Path){
            operator = Ops.EQ_OBJECT;
            args = new Expression[]{args[1],args[0]};

        }else if (operator == Ops.ORDINAL){
            List<MappedPredicate> predicates = Collections.singletonList(new MappedPredicate(CORE.enumOrdinal, false));
            SimplePath ordinalPath = new SimplePath(Integer.class, (Path)operation.getArg(0), "ordinal");
            operatorStack.pop();
            QStatement stmt = getProperty(query, (Path)operation.getArg(0), ordinalPath, predicates);
            return getSymbol(query, stmt, ordinalPath);
        }
        operatorStack.pop();
        if (operation.getType().equals(Boolean.class)){
            return BooleanOperation.create(operator, args);
        }else if (operation.getType().equals(String.class)){
            return StringOperation.create(operator, args);
        }else{
            return SimpleOperation.create(operation.getType(), operator, args);
        }
    }

    private Expression<?> transform(SQLCommonQuery<?> query, Path<?> path, boolean realType){
        PathType pathType = path.getMetadata().getPathType();
        if (path.getMetadata().getParent() != null){
            transform(query, path.getMetadata().getParent(), false);
        }
        if (pathType == PathType.VARIABLE || variables.containsKey(path)){
            QStatement stmt = getVariable(query, path);
            if (realType){
                return getSymbol(query, stmt, path);
            }else{
                return stmt.subject;
            }

        }else if (pathType == PathType.PROPERTY){
            QStatement stmt = getProperty(query, path.getMetadata().getParent(), path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return inversePaths.contains(path) ? stmt.subject : stmt.object;
            }

        }else if (pathType == PathType.COLLECTION_ANY){
            return transform(query, path.getMetadata().getParent(), realType);

        }else{
            throw new IllegalArgumentException("Unsupported path type " + pathType);
        }
    }

    private Expression<?> transform(SQLCommonQuery<?> query, SubQueryExpression<?> expr, boolean realType) {
        SubQueryExpression<?> q = expr;
        List<? extends Expression<?>> p = q.getMetadata().getProjection();
        SQLSubQuery sq = new SQLSubQuery();
        Expression<?>[] projection = populate(sq, q.getMetadata(), p.toArray(new Expression[p .size()]));
        return sq.list(projection);
    }

    @Override
    public Object[] uniqueResult(Expression<?>[] args) {
        SQLQuery query = context.createQuery();
        return query.uniqueResult(populate(query, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> RT uniqueResult(Expression<RT> arg) {
        SQLQuery query = context.createQuery();
        return (RT) query.uniqueResult(populate(query, arg)[0]);
    }

}
