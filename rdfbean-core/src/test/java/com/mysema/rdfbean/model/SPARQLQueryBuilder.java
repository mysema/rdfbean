package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.Namespaces;

public class SPARQLQueryBuilder {

    // TODO : union

    private final Map<String,String> knownPrefixes;

    private UID base;

    private final Set<String> usedNamespaces = new HashSet<String>();

    private final List<String> select = new ArrayList<String>();

    private boolean distinct, reduced;

    private final List<String> construct = new ArrayList<String>();

    private final List<UID> from = new ArrayList<UID>();

    private final List<UID> fromNamed = new ArrayList<UID>();

    private final List<String> where = new ArrayList<String>();

    private final List<String> order = new ArrayList<String>();

    private Integer limit, offset;

    public SPARQLQueryBuilder() {
        this.knownPrefixes = Namespaces.DEFAULT;
    }

    public SPARQLQueryBuilder(Map<String,String> namespaces) {
        this.knownPrefixes = namespaces;
    }

    public SPARQLQueryBuilder base(UID uid){
        base = uid;
        return this;
    }

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

    public SPARQLQueryBuilder graph(Object c, String... patterns){
        StringBuilder builder = new StringBuilder("GRAPH ");
        builder.append(str(c));
        builder.append(" { ").append(StringUtils.join(patterns, " . ")).append(" }");
        where.add(builder.toString());
        return this;
    }

    public SPARQLQueryBuilder ns(String namespace){
        usedNamespaces.add(namespace);
        return this;
    }

    public SPARQLQueryBuilder where(String... w){
        where.addAll(Arrays.asList(w));
        return this;
    }

    public SPARQLQueryBuilder where(Object s, Object p, Object o){
        where.add(pattern(s, p, o));
        return this;
    }

    public SPARQLQueryBuilder where(Object s, Object p, Object o, Object c){
        where.add(pattern(s, p, o, c));
        return this;
    }

    public SPARQLQueryBuilder orderBy(String... o){
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

    public SPARQLQueryBuilder limit(Integer l){
        this.limit = l;
        return this;
    }

    public SPARQLQueryBuilder offset(Integer o){
        this.offset = o;
        return this;
    }

    public SPARQLQueryBuilder filter(String filter) {
        if (filter.contains("(") && filter.contains(")")){
            where.add("FILTER " + filter);
        }else{
            where.add("FILTER (" + filter + ")");
        }
        return this;
    }

    public SPARQLQueryBuilder optional(String... str){
        StringBuilder builder = new StringBuilder("OPTIONAL { ");
        builder.append(StringUtils.join(str, " . "));
        builder.append(" }");
        where.add(builder.toString());
        return this;
    }

    public SPARQLQueryBuilder optional(Object s, Object p, Object o){
        where.add("OPTIONAL { " + pattern(s, p, o) + " }");
        return this;
    }

    private String pattern(Object s, Object p, Object o){
        return str(s) + " " + str(p) + " " + str(o);
    }

    private String pattern(Object s, Object p, Object o, Object c){
        return "GRAPH " + str(c) + " { " + pattern(s, p, o) + " }";
    }

    private String str(Object n){
        if (n instanceof NODE){
            return str((NODE)n);
        }else{
            return n.toString();
        }
    }

    private String str(NODE node){
        if (node.isURI()){
            return str(node.asURI());
        }else if (node.isBNode()){
            return "_:" + node.getValue();
        }else{
            return str(node.asLiteral());
        }
    }

    private String str(UID uid){
        String prefix = knownPrefixes.get(uid.ns());
        if (prefix != null){
            usedNamespaces.add(uid.ns());
            return prefix + ":" + uid.ln();
        }else{
            return "<" + uid.getValue() + ">";
        }
    }

    private String str(LIT lit){
        String value = "\"" + lit.getValue() + "\"";
        if (lit.getLang() != null){
            return value + "@" + LocaleUtil.toLang(lit.getLang());
        }else if (lit.getDatatype() != null && !lit.getDatatype().equals(XSD.stringType)){
            return value + "^^" + str(lit.getDatatype());
        }else{
            return value;
        }
    }

    @Override
    public String toString(){
        StringBuilder out = new StringBuilder();
        // base
        if (base != null){
            out.append("BASE <").append(base.getId()).append(">\n");
        }
        // prefixes
        for (String ns : usedNamespaces){
            out.append("PREFIX ").append(knownPrefixes.get(ns)).append(": <").append(ns).append(">\n");
        }
        // select
        if (!select.isEmpty()){
            out.append("SELECT ");
            if (distinct){
                out.append("DISTINCT ");
            }else if (reduced){
                out.append("REDUCED ");
            }
            out.append(StringUtils.join(select, " "));
            out.append("\n");
        }
        // from
        for (UID uid : from){
            out.append("FROM <").append(uid.getId()).append(">\n");
        }
        // from named
        for (UID uid : fromNamed){
            out.append("FROM NAMED <").append(uid.getId()).append(">\n");
        }
        // where
        if (!where.isEmpty()){
            out.append("WHERE {\n  ");
            out.append(StringUtils.join(where, " . \n  "));
            out.append(" . }\n");
        }
        // order
        if (!order.isEmpty()){
            out.append("ORDER BY ");
            out.append(StringUtils.join(order, " "));
            out.append("\n");
        }
        // limit
        if (limit != null){
            out.append("LIMIT ").append(limit).append("\n");
        }
        // offset
        if (offset != null){
            out.append("OFFSET ").append(offset).append("\n");
        }
        return out.toString();
    }

}
