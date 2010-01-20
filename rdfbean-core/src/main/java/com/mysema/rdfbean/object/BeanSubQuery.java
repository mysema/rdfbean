package com.mysema.rdfbean.object;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMixin;
import com.mysema.query.support.DetachableQuery;
import com.mysema.query.types.path.PEntity;

/**
 * BeanSubQuery provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQuery extends DetachableQuery<BeanSubQuery>{
    
    public BeanSubQuery() {
        super(new QueryMixin<BeanSubQuery>(new DefaultQueryMetadata()));
        queryMixin.setSelf(this);
    }
    
    public BeanSubQuery from(PEntity<?>... o) {
        return queryMixin.from(o);
    }

}
