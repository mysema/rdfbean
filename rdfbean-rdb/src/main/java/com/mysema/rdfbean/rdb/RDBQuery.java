package com.mysema.rdfbean.rdb;

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
    
    private final Map<Path<?>,QStatement> variables = new HashMap<Path<?>,QStatement>();
    
    private final Map<Path<?>,QStatement> properties = new HashMap<Path<?>,QStatement>();
    
    private SQLQuery query;
    
    public RDBQuery(RDBContext context, Session session) {
        super(new QueryMixin<RDBQuery>());
        this.context = context;
        this.configuration = session.getConfiguration();
        this.session = session;
    }

    @Override
    public long count() {
        return createQuery().count();
    }

    @SuppressWarnings("unchecked")
    public SQLQuery createQuery(){
        query = context.createQuery();
        QueryMetadata md = queryMixin.getMetadata();
        // from
        for (JoinExpression join : md.getJoins()){
            Expr<?> target = join.getTarget();
            getVariable((Path<?>) target);
        }
        // where
        if (md.getWhere() != null){
            query.where(transform(md.getWhere()));
        }
        // group by
        for (Expr<?> expr : md.getGroupBy()){
            query.groupBy(transform(expr));
        }
        // having
        if (md.getHaving() != null){
            query.having(transform(md.getHaving()));
        }
        // order
        for (OrderSpecifier<?> order : md.getOrderBy()){
            query.orderBy(new OrderSpecifier(order.getOrder(), transform(order.getTarget())));
        }        
        // paging
        if (md.getModifiers() != null){
            query.restrict(md.getModifiers());
        }        
        // select
        return query;
    }

    private QStatement getVariable(Path<?> target) {
        MappedClass mc = configuration.getMappedClass(target.getType());
        QStatement statement = new QStatement(target.toString());
        variables.put(target, statement);
        query.from(statement);
        query.where(statement.predicate.eq(getId(RDF.type)));
        query.where(statement.object.eq(getId(mc.getUID())));
        return statement;
    }
    
    private QStatement getProperty(Path<?> parent, Path<?> target){
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
    }

    @Override
    public BeanQuery from(PEntity<?>... o) {
        queryMixin.from(o);
        return this;
    }
    
    private Long getId(NODE node) {
        return context.getNodeId(node);
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.iterate(projection(args));
    }
    
    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.iterate(projection(projection));
    }
        
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.list(projection(args));
    }

    @Override
    public <RT> List<RT> list(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.list(projection(projection));
    }

    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.listResults(projection(projection));
    }

    private Expr<?>[] projection(Expr<?>[] exprs) {
        // TODO
        return null;
    }

    private <T> Expr<T> projection(Expr<T> expr) {
        // TODO
        return null;
    }
    
    private EBoolean transform(EBoolean filter) {
        if (filter instanceof Path<?>){
            // TODO
            return null;
        }else if (filter instanceof Operation<?>){
            Operation<?> op = (Operation<?>)filter;
            return OBoolean.create((Operator)op.getOperator(), transform(query, op.getArgs()));
        }else if (filter instanceof Custom<?>){
            Custom<?> c = (Custom<?>)filter;
            return CBoolean.create(c.getTemplate(), transform(query, c.getArgs()));
        }else if (filter instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)filter;
            return transform(bb.getValue());
        }else{
            return filter;    
        }
    }

    private Expr<?> transform(Expr<?> expr) {
        if (expr instanceof Path<?>){
            if (variables.containsKey(expr)){
                return variables.get(expr).subject;
            }else if (properties.containsKey(expr)){    
                return properties.get(expr).object;                
            }else{
                Path<?> path = (Path<?>)expr;
                PathType pathType = path.getMetadata().getPathType();
                if (pathType == PathType.VARIABLE){
                    return getVariable(path);
                }else if (pathType == PathType.PROPERTY){
                    transform(path.getMetadata().getParent().asExpr());
                    return getProperty(path.getMetadata().getParent(), path);
                }else{
                    throw new IllegalArgumentException("Unsupported path type " + pathType);
                }
            }
        }else if (expr instanceof Operation<?>){
            Operation<?> op = (Operation<?>)expr;
            return OSimple.create(op.getType(), (Operator)op.getOperator(), transform(query, op.getArgs()));
        }else if (expr instanceof Custom<?>){
            Custom<?> c = (Custom<?>)expr;
            return CSimple.create(c.getType(), c.getTemplate(), transform(query, c.getArgs()));
        }else if (expr instanceof BooleanBuilder){
            BooleanBuilder bb = (BooleanBuilder)expr;
            return transform(bb.getValue());
        }else{
            return expr;    
        }
    }
    
    private Expr<?>[] transform(SQLQuery query, List<Expr<?>> exprs) {
        Expr<?>[] rv = new Expr[exprs.size()];
        for (int i = 0; i < rv.length; i++){
            rv[i] = transform(exprs.get(i));
        }
        return rv;
    }

    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.uniqueResult(projection(args));
    }
    
    @Override
    public <RT> RT uniqueResult(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return (RT) query.uniqueResult(projection(projection));
    }

    @Override
    public String toString(){
        return createQuery().toString();
    }
}
