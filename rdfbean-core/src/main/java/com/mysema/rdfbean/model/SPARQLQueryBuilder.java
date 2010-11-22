package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SPARQLQueryBuilder {

    private final List<String> select = new ArrayList<String>();

    private boolean distinct, reduced;

    private final List<String> construct = new ArrayList<String>();

    private final List<UID> from = new ArrayList<UID>();

    private final List<UID> fromNamed = new ArrayList<UID>();

    private final StringBuilder where = new StringBuilder();

    private final List<String> order = new ArrayList<String>();

    private Long limit, offset;

    public SPARQLQueryBuilder select(String... variables){
        select.addAll(Arrays.asList(variables));
        return this;
    }

    public SPARQLQueryBuilder construct(String... patterns){
        construct.addAll(Arrays.asList(patterns));
        return this;
    }

    public SPARQLQueryBuilder from(UID... uris){
        from.addAll(Arrays.asList(uris));
        return this;
    }

    public SPARQLQueryBuilder fromNamed(UID... uris){
        fromNamed.addAll(Arrays.asList(uris));
        return this;
    }

    public SPARQLQueryBuilder where(String... constraints){
        for (String c : constraints){
            where.append(c).append(" . ");
        }
        return this;
    }

    public SPARQLQueryBuilder order(String... o){
        order.addAll(Arrays.asList(o));
        return this;
    }

    public SPARQLQueryBuilder distinct(){
        distinct = true;
        return this;
    }

    public SPARQLQueryBuilder reduced(){
        reduced = true;
        return this;
    }

    public SPARQLQueryBuilder limit(Long l){
        this.limit = l;
        return this;
    }

    public SPARQLQueryBuilder offset(Long o){
        this.offset = o;
        return this;
    }

    @Override
    public String toString(){
        // TODO
        return "";
    }
}
