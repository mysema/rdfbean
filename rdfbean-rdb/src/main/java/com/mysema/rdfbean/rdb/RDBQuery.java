package com.mysema.rdfbean.rdb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.SearchResults;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Custom;
import com.mysema.query.types.EConstructor;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathType;
import com.mysema.query.types.custom.CBoolean;
import com.mysema.query.types.custom.CSimple;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.OBoolean;
import com.mysema.query.types.expr.OSimple;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PSimple;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class RDBQuery extends ProjectableQuery<RDBQuery> implements BeanQuery{
    
    private final RDBContext context;
    
    private final Configuration configuration;
    
    private final Session session;
    
    // $var rdf:type ?
    private final Map<Path<?>,QStatement> variables = new HashMap<Path<?>,QStatement>();
    
    // ? ? $prop
    private final Map<Path<?>,QStatement> properties = new HashMap<Path<?>,QStatement>();
    
    private final Map<Path<?>,Expr<?>> symbols = new HashMap<Path<?>,Expr<?>>();
    
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
        return createQuery().count();
    }

    @SuppressWarnings("unchecked")
    public SQLQuery createQuery(){
        SQLQuery query = context.createQuery();
        QueryMetadata md = queryMixin.getMetadata();
        // from
        for (JoinExpression join : md.getJoins()){
            Expr<?> target = join.getTarget();
            getVariable(query,(Path<?>) target);
        }
        // where
        if (md.getWhere() != null){
            query.where(transform(query,md.getWhere()));
        }
        // group by
        for (Expr<?> expr : md.getGroupBy()){
            query.groupBy(transform(query,expr,false));
        }
        // having
        if (md.getHaving() != null){
            query.having(transform(query,md.getHaving()));
        }
        // order
        for (OrderSpecifier<?> order : md.getOrderBy()){
            query.orderBy(new OrderSpecifier(order.getOrder(), transform(query,order.getTarget(),true)));
        }        
        // paging
        if (md.getModifiers() != null){
            query.restrict(md.getModifiers());
        }        
        // select
        return query;
    }
    
    @Override
    public BeanQuery from(PEntity<?>... o) {
        queryMixin.from(o);
        return this;
    }

    private Long getId(NODE node) {
        return context.getNodeId(node);
    }
    
    private QStatement getProperty(SQLQuery query, Path<?> parent, Path<?> target){
        if (!properties.containsKey(target)){
            MappedClass mc = configuration.getMappedClass(parent.getType());
            QStatement statement = new QStatement(target.toString().replace('.', '_'));
            properties.put(target, statement);
            if (variables.containsKey(parent)){
                // property of variable            
                QStatement parentStmt = variables.get(parent);
                query.innerJoin(statement).on(parentStmt.subject.eq(statement.subject));            
            }else if (properties.containsKey(parent)){
                QStatement parentStmt = properties.get(parent);
                query.innerJoin(statement).on(parentStmt.object.eq(statement.subject));
            }
            MappedPath mp = mc.getMappedPath(target.getMetadata().getExpression().toString());
            // TODO : support longer paths
            query.where(statement.predicate.eq(getId(mp.getPredicatePath().get(0).getUID())));    
            return statement;    
        }else{
            return properties.get(target);
        }        
    }
    
    private Expr<?> getSymbol(SQLQuery query, QStatement stmt, Path<?> path) {
        if (!symbols.containsKey(path)){
            QSymbol symbol = new QSymbol(stmt + "_symbol_");
            if (path.getMetadata().isRoot()){
                query.innerJoin(stmt.subjectFk, symbol);
            }else{
                query.innerJoin(stmt.objectFk, symbol);    
            }            
            Expr<?> expr;
            if (context.isDecimalClass(path.getType())){
                expr = cast(symbol.floating,path.getType());
            }else if (Number.class.isAssignableFrom(path.getType())){
                expr = cast(symbol.integer,path.getType());
            }else if (Date.class.isAssignableFrom(path.getType())){
                expr = symbol.datetime;
            }else{
                expr = symbol.lexical;
            }symbols.put(path, expr);
            return expr;
        }else{
            return symbols.get(path);
        }
    }

    private QStatement getVariable(SQLQuery query, Path<?> target) {
        if (!variables.containsKey(target)){
            MappedClass mc = configuration.getMappedClass(target.getType());
            QStatement statement = new QStatement(target.toString());
            variables.put(target, statement);
            query.from(statement);
            query.where(statement.predicate.eq(getId(RDF.type)));
            query.where(statement.object.eq(getId(mc.getUID())));
            return statement;            
        }else{
            return variables.get(target);    
        }        
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.iterate(projection(query, args));
    }
    
    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.iterate(projection(query, projection));
    }
        
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.list(projection(query, args));
    }

    @Override
    public <RT> List<RT> list(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.list(projection(query, projection));
    }

    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.listResults(projection(query, projection));
    }

    private Expr<?>[] projection(SQLQuery query, Expr<?>[] exprs) {
        Expr<?>[] rv = new Expr<?>[exprs.length];
        for (int i = 0; i < rv.length; i++){
            rv[i] = projection(query, exprs[i]);
        }
        return rv;
    }

    @SuppressWarnings({ "unchecked", "serial" })
    private <T> Expr<T> projection(SQLQuery query, final Expr<T> expr) {
        if (expr instanceof Path<?> && expr.getType().getAnnotation(ClassMapping.class) != null){
            Expr<?> source = (Expr<T>) transform(query, (Path<?>)expr, true);
            return new EConstructor<T>((Class)expr.getType(), new Class[0], source){
                @Override
                public T newInstance(Object... args){
                    String id = args[0].toString();
                    return session.get(expr.getType(), context.getID(id, !id.startsWith("_")));
                }
            };
        }else{
            return (Expr<T>) transform(query, expr, true);
        }
    }
    
    @Override
    public String toString(){
        return createQuery().toString();
    }
    
    @SuppressWarnings("unchecked")
    private EBoolean transform(SQLQuery query, EBoolean filter) {
        if (filter instanceof Path<?>){
            throw new IllegalArgumentException(filter.toString());
        }else if (filter instanceof Operation<?>){
            Operation<?> op = (Operation<?>)filter;
//            boolean realType = !Ops.equalsOps.contains(op.getOperator()) && !Ops.notEqualsOps.contains(op.getOperator());
            return OBoolean.create((Operator)op.getOperator(), transform(query, op.getArgs(), true));
        }else if (filter instanceof Custom<?>){
            Custom<?> c = (Custom<?>)filter;
            return CBoolean.create(c.getTemplate(), transform(query, c.getArgs(), true));
        }else if (filter instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)filter;
            return transform(query,bb.getValue());
        }else{
            return filter;    
        }
    }

    @SuppressWarnings("unchecked")
    private Expr<?> transform(SQLQuery query, Expr<?> expr, boolean realType) {
        if (expr instanceof Path<?>){
            return transform(query, (Path<?>)expr, realType);
        }else if (expr instanceof Operation<?>){
            Operation<?> op = (Operation<?>)expr;
//            boolean rt = !Ops.equalsOps.contains(op.getOperator()) && !Ops.notEqualsOps.contains(op.getOperator());
            return OSimple.create(op.getType(), (Operator)op.getOperator(), transform(query, op.getArgs(), true));
        }else if (expr instanceof Custom<?>){
            Custom<?> c = (Custom<?>)expr;
            return CSimple.create(c.getType(), c.getTemplate(), transform(query, c.getArgs(), true));
        }else if (expr instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)expr;
            return transform(query,bb.getValue());
        }else{
            return expr;    
        }
    }
    
    private Expr<?>[] transform(SQLQuery query, List<Expr<?>> exprs, boolean realType) {
        Expr<?>[] rv = new Expr[exprs.size()];
        for (int i = 0; i < rv.length; i++){
            rv[i] = transform(query, exprs.get(i), realType);
        }
        return rv;
    }

    private Expr<?> transform(SQLQuery query, Path<?> path, boolean realType){
        PathType pathType = path.getMetadata().getPathType();
        if (pathType == PathType.VARIABLE){
            QStatement stmt = getVariable(query,path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return stmt;
            }
        }else if (pathType == PathType.PROPERTY){
            transform(query,path.getMetadata().getParent().asExpr(), realType);
            QStatement stmt = getProperty(query,path.getMetadata().getParent(), path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return stmt;
            }            
        }else{
            throw new IllegalArgumentException("Unsupported path type " + pathType);
        }
    }
    
    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.uniqueResult(projection(query, args));
    }

    @Override
    public <RT> RT uniqueResult(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return (RT) query.uniqueResult(projection(query, projection));
    }
}
