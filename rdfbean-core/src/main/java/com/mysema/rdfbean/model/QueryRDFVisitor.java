package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.AndPredicate;
import org.apache.commons.collections15.functors.AnyPredicate;
import org.apache.commons.collections15.functors.NotPredicate;
import org.apache.commons.collections15.iterators.FilterIterator;
import org.apache.commons.collections15.iterators.IteratorChain;
import org.apache.commons.collections15.iterators.TransformIterator;
import org.apache.commons.lang.ObjectUtils;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.*;


/**
 * @author tiwe
 *
 */
public class QueryRDFVisitor implements RDFVisitor<Object, QueryMetadata>{

    private static final NODEComparator nodeComparator = new NODEComparator();

    private final RDFConnection connection;

    private final Map<String, NODE> bindings;

    public QueryRDFVisitor(RDFConnection connection) {
        this.connection = connection;
        this.bindings = new HashMap<String, NODE>();
    }

    @Override
    public Object visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        final Iterable<Map<String, NODE>> iterable = (Iterable<Map<String, NODE>>) md.getWhere().accept(this, md);

        // GRAPH
        if (queryType == QueryLanguage.GRAPH){
            if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof PatternBlock){
                final Transformer<Map<String,NODE>, STMT> transformer =
                    createStatementTransformer((PatternBlock)md.getProjection().get(0), md);
                return new GraphQuery(){
                    @Override
                    public CloseableIterator<STMT> getTriples() {
                        return new IteratorAdapter<STMT>(
                                new TransformIterator<Map<String, NODE>, STMT>(iterable.iterator(), transformer));
                    }
                };
            }else{
                throw new IllegalArgumentException(md.getProjection().toString());
            }

        // TUPLE
        }else if (queryType == QueryLanguage.TUPLE){
            final List<String> variables = new ArrayList<String>(md.getProjection().size());
            for (Expression<?> expr : md.getProjection()){
                String key = getKey(expr);
                variables.add(key != null ? key : expr.toString());
            }
            return new TupleQuery(){
                @Override
                public CloseableIterator<Map<String, NODE>> getTuples() {
                    return new IteratorAdapter<Map<String,NODE>>(iterable.iterator());
                }
                @Override
                public List<String> getVariables() {
                    return variables;
                }
            };

        // BOOLEAN
        }else if (queryType == QueryLanguage.BOOLEAN){
            return new BooleanQuery(){
                @Override
                public boolean getBoolean() {
                    return iterable.iterator().hasNext();
                }
            };

        }else{
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(UnionBlock expr, QueryMetadata context) {
        final List<Iterable<Map<String,NODE>>> iterables = new ArrayList<Iterable<Map<String,NODE>>>();
        for (Block block : expr.getBlocks()){
            iterables.add((Iterable) block.accept(this, context));
        }
        return new Iterable<Map<String,NODE>>() {
            @Override
            public Iterator<Map<String, NODE>> iterator() {
                IteratorChain<Map<String,NODE>> chain = new IteratorChain<Map<String,NODE>>();
                for (Iterable iterable : iterables){
                    chain.addIterator(iterable.iterator());
                }
                return chain;
            }
        };
    }

    @Override
    public Object visit(GroupBlock expr, QueryMetadata context) {
        return visit((ContainerBlock)expr, context);
    }

    @Override
    public Object visit(GraphBlock expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(OptionalBlock expr, QueryMetadata context) {
        // TODO : handle optional correctly
        return visit((ContainerBlock)expr, context);
    }

    private Iterable<Map<String,NODE>> visit(ContainerBlock expr, QueryMetadata context){
        List<Iterable<Map<String,NODE>>> iterables = new ArrayList<Iterable<Map<String,NODE>>>();
        for (Block block : expr.getBlocks()){
            iterables.add((Iterable) block.accept(this, context));
        }

        // TODO : merge
        final Iterable<Map<String,NODE>> iterable = iterables.get(0);

        if (expr.getFilters() != null){
            final Predicate<Map<String,NODE>> predicate = (Predicate) expr.getFilters().accept(this, context);
            return new Iterable<Map<String, NODE>>(){
                @Override
                public Iterator<Map<String, NODE>> iterator() {
                    return new FilterIterator<Map<String, NODE>>(iterable.iterator(), predicate);
                }
            };
        }else{
            return iterable;
        }
    }

    @Override
    public Iterable<Map<String, NODE>> visit(final PatternBlock expr, final QueryMetadata context) {
        final Transformer<STMT, Map<String, NODE>> transformer = createBindingsTransformer(expr);
        return new Iterable<Map<String,NODE>>(){
            @Override
            public Iterator<Map<String,NODE>> iterator() {
                ID s = (ID) expr.getSubject().accept(QueryRDFVisitor.this, context);
                UID p = (UID) expr.getPredicate().accept(QueryRDFVisitor.this, context);
                NODE o = (NODE) expr.getObject().accept(QueryRDFVisitor.this, context);
                UID c = null;
                if (expr.getContext() != null){
                    c = (UID) expr.getContext().accept(QueryRDFVisitor.this, context);
                }
                return new TransformIterator<STMT, Map<String, NODE>>(connection.findStatements(s, p, o, c, false), transformer);
            }
        };
    }

    private Transformer< Map<String, NODE>, STMT> createStatementTransformer(
            final PatternBlock expr, final QueryMetadata context){
        return new Transformer<Map<String,NODE>, STMT>(){
            @Override
            public STMT transform(Map<String, NODE> input) {
                if (expr.getContext() != null){
                    return new STMT(
                            (ID)expr.getSubject().accept(QueryRDFVisitor.this, context),
                            (UID)expr.getPredicate().accept(QueryRDFVisitor.this, context),
                            (NODE)expr.getObject().accept(QueryRDFVisitor.this, context)
                    );
                }else{
                    return new STMT(
                            (ID)expr.getSubject().accept(QueryRDFVisitor.this, context),
                            (UID)expr.getPredicate().accept(QueryRDFVisitor.this, context),
                            (NODE)expr.getObject().accept(QueryRDFVisitor.this, context),
                            (UID)expr.getContext().accept(QueryRDFVisitor.this, context)
                    );
                }

            }
        };
    }

    private Transformer<STMT, Map<String, NODE>> createBindingsTransformer(PatternBlock expr){
        final String s = getKey(expr.getSubject());
        final String p = getKey(expr.getPredicate());
        final String o = getKey(expr.getObject());
        final String c = getKey(expr.getContext());
        return new Transformer<STMT, Map<String, NODE>>(){
            @Override
            public Map<String, NODE> transform(STMT input) {
                bind(s, input.getSubject());
                bind(p, input.getPredicate());
                bind(o, input.getObject());
                bind(c, input.getContext());
                return bindings;
            }
        };
    }

    private void bind(String key, NODE value) {
        if (key != null){
            bindings.put(key, value);
        }
    }

    private String getKey(Expression<?> expr){
        if (expr instanceof Path<?>){
            return expr.toString();
        }else if (expr instanceof ParamExpression<?>){
            return ((ParamExpression<?>)expr).getName();
        }else{
            return null;
        }
    }

    @Override
    public NODE visit(Constant<?> expr, QueryMetadata context) {
        return (NODE) expr.getConstant();
    }

    @Override
    public Object visit(FactoryExpression<?> expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate visit(final Operation<?> expr, final QueryMetadata context) {
        final Operator<?> op = expr.getOperator();
        if (op == Ops.EQ_OBJECT || op == Ops.EQ_PRIMITIVE){
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    return ObjectUtils.equals(
                            expr.getArg(0).accept(QueryRDFVisitor.this, context),
                            expr.getArg(1).accept(QueryRDFVisitor.this, context));
                }
            };
        }else if (op == Ops.NE_OBJECT || op == Ops.NE_PRIMITIVE){
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    return !ObjectUtils.equals(
                            expr.getArg(0).accept(QueryRDFVisitor.this, context),
                            expr.getArg(1).accept(QueryRDFVisitor.this, context));
                }
            };
        }else if (op == Ops.AND){
            return new AndPredicate(
                    (Predicate)expr.getArg(0).accept(this, context),
                    (Predicate)expr.getArg(1).accept(this, context));

        }else if (op == Ops.OR){
            return new AnyPredicate( new Predicate[]{
                    (Predicate)expr.getArg(0).accept(this, context),
                    (Predicate)expr.getArg(1).accept(this, context)});

        }else if (op == Ops.NOT){
            return new NotPredicate( (Predicate) expr.getArg(0).accept(this, context));

        }else if (op == Ops.IS_NULL || op == Ops.IS_NOT_NULL){
            final String key = getKey(expr.getArg(0));
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    boolean rv =  bindings.get(key) != null;
                    return op == Ops.IS_NOT_NULL ? rv : !rv;
                }
            };

        }else if (op == Ops.IS_NOT_NULL){
            final String key = getKey(expr.getArg(0));
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    return bindings.get(key) != null;
                }
            };

        }else if (op == Ops.LT || op == Ops.GT || op == Ops.LOE || op == Ops.GOE){
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    NODE lhs = (NODE)expr.getArg(0).accept(QueryRDFVisitor.this, context);
                    NODE rhs = (NODE)expr.getArg(1).accept(QueryRDFVisitor.this, context);
                    int rv = nodeComparator.compare(lhs, rhs);
                    if (rv < 0){
                        return op == Ops.LT || op == Ops.LOE;
                    }else if (rv == 0){
                        return op == Ops.LOE || op == Ops.GOE;
                    }else{
                        return op == Ops.GT || op == Ops.GOE;
                    }
                }
            };

        }else{
            throw new IllegalArgumentException(expr.toString());
        }
    }

    @Override
    public NODE visit(ParamExpression<?> expr, QueryMetadata context) {
        return (NODE) context.getParams().get(expr);
    }

    @Override
    public NODE visit(Path<?> expr, QueryMetadata context) {
        return bindings.get(expr.getMetadata().getExpression().toString());
    }

    @Override
    public Object visit(SubQueryExpression<?> expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(TemplateExpression<?> expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

}
