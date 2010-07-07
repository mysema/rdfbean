package com.mysema.rdfbean.rdb;

import java.util.List;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.SearchResults;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expr;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class RDBQuery extends ProjectableQuery<RDBQuery> implements BeanQuery{

    private final RDBContext context;
    
    private final Session session;
    
    public RDBQuery(RDBContext context, Session session) {
        super(new QueryMixin<RDBQuery>());
        this.context = context;
        this.session = session;
    }

    @Override
    public long count() {
        return createQuery().count();
    }

    private SQLQuery createQuery(){
        // from
        // where
        // group by
        // having
        // order
        // select
        return context.createQuery();
    }

    @Override
    public BeanQuery from(PEntity<?>... o) {
        queryMixin.from(o);
        return this;
    }

    @Override
    public CloseableIterator<Object[]> iterate(Expr<?>[] args) {
        return createQuery().iterate(args);
    }

    @Override
    public <RT> CloseableIterator<RT> iterate(Expr<RT> projection) {
        return createQuery().iterate(projection);
    }
    
    @Override
    public List<Object[]> list(Expr<?>[] args) {
        return createQuery().list(args);
    }

    @Override
    public <RT> List<RT> list(Expr<RT> projection) {
        return createQuery().list(projection);
    }
    
    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> projection) {
        return createQuery().listResults(projection);
    }

    @Override
    public Object[] uniqueResult(Expr<?>[] args) {
        return createQuery().uniqueResult(args);
    }
    
    @Override
    public <RT> RT uniqueResult(Expr<RT> expr) {
        return createQuery().uniqueResult(expr);
    }

}
