/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryBase;
import com.mysema.query.QueryMixin;
import com.mysema.query.paging.CallbackService;
import com.mysema.query.paging.ListSource;
import com.mysema.query.paging.ListSourceBase;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * PagedQuery provides an RDFBeanQuery style query builder for paged results
 *
 * @author tiwe
 * @version $Id$
 */
public class PagedBeanQuery extends QueryBase<PagedBeanQuery>{

    private final CallbackService txCallback;
    
    private final SessionFactory sessionFactory;
    
    private final Session session;
    
    private final List<PEntity<?>> sources = new ArrayList<PEntity<?>>();
    
    public PagedBeanQuery(CallbackService txCallback, 
            SessionFactory sessionFactory,
            Session session){
        super(new QueryMixin<PagedBeanQuery>(new DefaultQueryMetadata()));
        this.queryMixin.setSelf(this);
        this.txCallback = txCallback;
        this.sessionFactory = sessionFactory;
        this.session = session;
    }
    
    public PagedBeanQuery from(PEntity<?>... from){
        for (PEntity<?> source : from){
            sources.add(source);    
        }        
        return this;
    }
    
    public <T> ListSource<T> list(final PEntity<T> projection){
        final EBoolean condition = queryMixin.getMetadata().getWhere();
        final OrderSpecifier<?>[] order = queryMixin.getMetadata().getOrderBy().toArray(new OrderSpecifier[0]);
        final PEntity<?>[] sourceArray = sources.toArray(new PEntity[0]);
        BeanQuery countQry = session.from(sourceArray);
        if (condition != null){
            countQry.where(condition);
        }
        long count = countQry.count();
        
        if (count > 0l){
            return new ListSourceBase<T>(txCallback, count){
                @Override
                protected List<T> getInnerResults(final int from, final int to) {
                    BeanQuery qry = sessionFactory.getCurrentSession().from(sourceArray)
                        .offset(from).limit(to - from)
                        .orderBy(order);
                    if (condition != null){
                        qry.where(condition);
                    }
                    return qry.list(projection);
                }            
            };   
        }else{
            return ListSourceBase.emptyResults();
        }
    }

}
