package com.mysema.rdfbean.rdb;

import java.util.List;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.SearchResults;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class RDBQuery extends ProjectableQuery<RDBQuery> implements BeanQuery{

    private final RDBContext context;
    
    private final Configuration configuration;
    
    private final Session session;
    
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
    private SQLQuery createQuery(){
        SQLQuery query = context.createQuery();
        QueryMetadata md = queryMixin.getMetadata();
        // from
        for (JoinExpression join : md.getJoins()){
            // TODO : transform joins
            query.from(join.getTarget());
        }
        // where
        if (md.getWhere() != null){
            query.where(transform(query, md.getWhere()));
        }
        // group by
        for (Expr<?> expr : md.getGroupBy()){
            query.groupBy(transform(query, expr));
        }
        // having
        if (md.getHaving() != null){
            query.having(transform(query,md.getHaving()));
        }
        // order
        for (OrderSpecifier<?> order : md.getOrderBy()){
            query.orderBy(new OrderSpecifier(order.getOrder(), transform(query,order.getTarget())));
        }        
        // paging
        if (md.getModifiers() != null){
            query.restrict(md.getModifiers());
        }        
        // select
        return query;
    }

    private Expr<?> transform(SQLQuery query, Expr<?> expr) {
        // TODO transform
        return expr;
    }

    private EBoolean transform(SQLQuery query, EBoolean filter) {
        // TODO transform
        return filter;
    }
    
    private <T> Expr<T> projection(SQLQuery query, Expr<T> expr) {
        // TODO transform
        return expr;
    }
    
    private Expr<?>[] projection(SQLQuery query, Expr<?>[] exprs) {
        Expr<?>[] rv = new Expr[exprs.length];
        for (int i = 0; i < rv.length; i++){
            rv[i] = projection(query, exprs[i]);
        }
        return rv;
    }

    @Override
    public BeanQuery from(PEntity<?>... o) {
        queryMixin.from(o);
        return this;
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.iterate(projection(query,args));
    }

    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.iterate(projection(query,projection));
    }
    
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.list(projection(query,args));
    }

    @Override
    public <RT> List<RT> list(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.list(projection(query,projection));
    }
    
    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return query.listResults(projection(query,projection));
    }

    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        SQLQuery query = createQuery();
        return query.uniqueResult(projection(query,args));
    }
    
    @Override
    public <RT> RT uniqueResult(Expr<RT> projection) {
        SQLQuery query = createQuery();
        return (RT) query.uniqueResult(projection(query,projection));
    }

}
