package com.mysema.rdfbean.rdb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
import com.mysema.query.types.Custom;
import com.mysema.query.types.EConstructor;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathType;
import com.mysema.query.types.SubQuery;
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
    
    private boolean optional = false;
    
    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();
    
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
        SQLQuery query = context.createQuery();
        populate(query);
        return query.count();
    }

    // simplify
    
    private Expr<?>[] populate(SQLCommonQuery<?> query, Expr<?>... projection){
        return populate(query, queryMixin.getMetadata(), projection);
    }
    
    @SuppressWarnings("unchecked")
    private Expr<?>[] populate(SQLCommonQuery<?> query, QueryMetadata metadata, Expr<?>... projection){
        Expr<?>[] rv;
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
        
        // projection        
        rv = projection(query, projection);
        
        // order
        for (OrderSpecifier<?> order : md.getOrderBy()){
            optional = true;
            query.orderBy(new OrderSpecifier(order.getOrder(), transform(query,order.getTarget(),true)));
            optional = false;
        }        
        
        // paging
        if (md.getModifiers() != null){
            query.restrict(md.getModifiers());
        }        
        
        return rv;
    }
    
    @Override
    public BeanQuery from(PEntity<?>... o) {
        queryMixin.from(o);
        return this;
    }

    private Long getId(NODE node) {
        return context.getNodeId(node);
    }
    
    private QStatement getProperty(SQLCommonQuery<?> query, Path<?> parent, Path<?> target){
        if (!properties.containsKey(target)){
            MappedClass mc = configuration.getMappedClass(parent.getType());
            QStatement statement = new QStatement(target.toString().replace('.', '_'));
            properties.put(target, statement);
            BooleanBuilder joinCondition = new BooleanBuilder();
            if (variables.containsKey(parent)){
                // property of variable            
                joinCondition.and(variables.get(parent).subject.eq(statement.subject));                            
            }else if (properties.containsKey(parent)){
                joinCondition.and(properties.get(parent).object.eq(statement.subject));
            }else{
                throw new IllegalStateException();
            }
            MappedPath mp = mc.getMappedPath(target.getMetadata().getExpression().toString());
            // TODO : support longer paths
            joinCondition.and(statement.predicate.eq(getId(mp.getPredicatePath().get(0).getUID())));
            if (inOptionalPath()){
                query.leftJoin(statement).on(joinCondition);
            }else{
                query.innerJoin(statement).on(joinCondition);    
            }                            
            return statement;    
        }else{
            return properties.get(target);
        }        
    }
    
    private Expr<?> getSymbol(SQLCommonQuery<?> query, QStatement stmt, Path<?> path) {
        if (!symbols.containsKey(path)){
            QSymbol symbol = new QSymbol(stmt + "_symbol_");
            ForeignKey<QSymbol> fk;
            if (path.getMetadata().isRoot()){
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

    private QStatement getVariable(SQLCommonQuery<?> query, Path<?> target) {
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
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, args);
        return query.iterate(projection);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, arg);
        return (CloseableIterator<RT>) query.iterate(projection[0]);
    }
        
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, args);
        return query.list(projection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> List<RT> list(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, arg);
        return (List<RT>) query.list(projection[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, arg);
        return (SearchResults<RT>) query.listResults(projection[0]);
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
        SQLQuery query = context.createQuery();
        populate(query);
        return query.toString();
    }
    
    @SuppressWarnings("unchecked")
    private EBoolean transform(SQLCommonQuery<?> query, EBoolean filter) {
        if (filter instanceof Path<?>){
            throw new IllegalArgumentException(filter.toString());
        }else if (filter instanceof Operation<?>){
            Operation<?> op = (Operation<?>)filter;
            operatorStack.push(op.getOperator());
            Expr<?>[] args = transform(query, op.getArgs(), true);
            operatorStack.pop();
            return OBoolean.create((Operator)op.getOperator(),args);
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
    private Expr<?> transform(SQLCommonQuery<?> query, Expr<?> expr, boolean realType) {
        if (expr instanceof Path<?>){
            return transform(query, (Path<?>)expr, realType);
        }else if (expr instanceof Operation<?>){
            Operation<?> op = (Operation<?>)expr;
            operatorStack.push(op.getOperator());
            Expr<?>[] args = transform(query, op.getArgs(), true);
            operatorStack.pop();
            return OSimple.create(op.getType(), (Operator)op.getOperator(), args);
        }else if (expr instanceof Custom<?>){
            Custom<?> c = (Custom<?>)expr;
            return CSimple.create(c.getType(), c.getTemplate(), transform(query, c.getArgs(), true));
        }else if (expr instanceof SubQuery){    
            SubQuery<?> q = (SubQuery<?>)expr;
            List<? extends Expr<?>> p = q.getMetadata().getProjection();
            SQLSubQuery sq = new SQLSubQuery();
            Expr[] projection = populate(sq, q.getMetadata(), p.toArray(new Expr[p .size()]));
            return sq.list(projection);            
        }else if (expr instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)expr;
            return transform(query,bb.getValue());
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

    private Expr<?> transform(SQLCommonQuery<?> query, Path<?> path, boolean realType){
        PathType pathType = path.getMetadata().getPathType();
        if (pathType == PathType.VARIABLE){
            QStatement stmt = getVariable(query,path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return stmt.subject;
            }
        }else if (pathType == PathType.PROPERTY){
            transform(query,path.getMetadata().getParent().asExpr(), false);
            QStatement stmt = getProperty(query,path.getMetadata().getParent(), path);
            if (realType){
                return getSymbol(query,stmt, path);
            }else{
                return stmt.object;
            }            
        }else{
            throw new IllegalArgumentException("Unsupported path type " + pathType);
        }
    }
    
    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, args);
        return query.uniqueResult(projection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> RT uniqueResult(Expr<RT> arg) {
        SQLQuery query = context.createQuery();
        Expr<?>[] projection = populate(query, arg);
        return (RT) query.uniqueResult(projection[0]);
    }
    
    private boolean inOptionalPath(){
        return optional 
            || operatorStack.contains(Ops.IS_NULL) 
            || (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
}
