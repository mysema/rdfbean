package com.mysema.rdfbean.object;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.Detachable;
import com.mysema.query.QueryMixin;
import com.mysema.query.support.DetachableMixin;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.query.ListSubQuery;
import com.mysema.query.types.query.ObjectSubQuery;

/**
 * BeanSubQuery provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQuery implements Detachable {
    
    private final QueryMixin<BeanSubQuery> queryMixin;
    
    private final DetachableMixin detachableMixin;
    
    public BeanSubQuery() {
        queryMixin = new QueryMixin<BeanSubQuery>(this, new DefaultQueryMetadata());
        detachableMixin = new DetachableMixin(queryMixin);
    }
    
    public BeanSubQuery from(PEntity<?>... o) {
        return queryMixin.from(o);
    }
        
    public BeanSubQuery orderBy(OrderSpecifier<?>... o){
        return queryMixin.orderBy(o);
    }
    
    public BeanSubQuery where(EBoolean... o){
        return queryMixin.where(o);
    }

    @Override
    public ObjectSubQuery<Long> count() {
        return detachableMixin.count();
    }

    @Override
    public EBoolean exists() {
        return detachableMixin.exists();
    }

    @Override
    public <RT> ListSubQuery<RT> list(Expr<RT> projection) {
        return detachableMixin.list(projection);
    }

    @Override
    public ListSubQuery<Object[]> list(Expr<?> first, Expr<?> second, Expr<?>... rest) {
        return detachableMixin.list(first, second, rest);
    }

    @Override
    public EBoolean notExists() {
        return detachableMixin.notExists();
    }

    @Override
    public <RT> ObjectSubQuery<RT> unique(Expr<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public ObjectSubQuery<Object[]> unique(Expr<?> first, Expr<?> second, Expr<?>... rest) {
        return detachableMixin.unique(first, second, rest);
    }

    @Override
    public ListSubQuery<Object[]> list(Expr<?>[] args) {
        return detachableMixin.list(args);
    }

    @Override
    public ObjectSubQuery<Object[]> unique(Expr<?>[] args) {
        return detachableMixin.unique(args);
    }
    

}
