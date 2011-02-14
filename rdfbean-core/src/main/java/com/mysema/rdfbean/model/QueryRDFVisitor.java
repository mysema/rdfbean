package com.mysema.rdfbean.model;

import java.util.ArrayList;
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
public class QueryRDFVisitor implements RDFVisitor<Object, Bindings>{

    private static final NODEComparator nodeComparator = new NODEComparator();

    private static final Transformer<Bindings, Map<String,NODE>> bindingsToMap = new Transformer<Bindings, Map<String,NODE>>(){
        @Override
        public Map<String, NODE> transform(Bindings input) {
            return input.toMap();
        }        
    };
    
    private final RDFConnection connection;

    
    public QueryRDFVisitor(RDFConnection connection) {
        this.connection = connection;
    }

    @Override
    public Object visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        Bindings initialBindings = new Bindings();
        for (Map.Entry<ParamExpression<?>, Object> entry : md.getParams().entrySet()){
            initialBindings.put(entry.getKey().getName(), (NODE) entry.getValue());
        }
        
        Bindings whereBindings = new Bindings(initialBindings);
        final Iterable<Bindings> iterable = (Iterable<Bindings>) md.getWhere().accept(this, whereBindings);

        // GRAPH
        if (queryType == QueryLanguage.GRAPH){
            if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof PatternBlock){
                final Transformer<Bindings, STMT> transformer = createStatementTransformer((PatternBlock)md.getProjection().get(0));
                return new GraphQuery(){
                    @Override
                    public CloseableIterator<STMT> getTriples() {
                        return new IteratorAdapter<STMT>(new TransformIterator<Bindings, STMT>(iterable.iterator(), transformer));
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
                    return new IteratorAdapter<Map<String, NODE>>(new TransformIterator<Bindings, Map<String,NODE>>(iterable.iterator(), bindingsToMap));
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
    public Iterable<Map<String, NODE>> visit(UnionBlock expr, Bindings context) {
        final List<Iterable<Map<String,NODE>>> iterables = new ArrayList<Iterable<Map<String,NODE>>>();
        for (Block block : expr.getBlocks()){
            iterables.add((Iterable) block.accept(this, new Bindings(context)));
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
    public Iterable<Bindings> visit(GroupBlock expr, Bindings context) {
        return visit((ContainerBlock)expr, context);
    }

    @Override
    public Iterable<Bindings> visit(GraphBlock expr, Bindings context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Bindings> visit(OptionalBlock expr, Bindings context) {
        // TODO : handle optional correctly
        return visit((ContainerBlock)expr, context);
    }

    private Iterable<Bindings> visit(ContainerBlock expr, Bindings context){
        List<Iterable<Bindings>> iterables = new ArrayList<Iterable<Bindings>>();
        Bindings blockContext = context;
        for (Block block : expr.getBlocks()){
            blockContext = new Bindings(blockContext);
            iterables.add((Iterable) block.accept(this, blockContext));
        }

        // TODO : merge
        final Iterable<Bindings> iterable = iterables.get(0);

        if (expr.getFilters() != null){
            final Predicate<Bindings> predicate = (Predicate) expr.getFilters().accept(this, blockContext);
            return new Iterable<Bindings>(){
                @Override
                public Iterator<Bindings> iterator() {
                    return new FilterIterator<Bindings>(iterable.iterator(), predicate);
                }
            };
        }else{
            return iterable;
        }
    }

    @Override
    public Iterable<Bindings> visit(final PatternBlock expr, final Bindings context) {
        final Transformer<STMT, Bindings> transformer = createBindingsTransformer(expr, context);
        return new Iterable<Bindings>(){
            @Override
            public Iterator<Bindings> iterator() {
                ID s = (ID) expr.getSubject().accept(QueryRDFVisitor.this, context);
                UID p = (UID) expr.getPredicate().accept(QueryRDFVisitor.this, context);
                NODE o = (NODE) expr.getObject().accept(QueryRDFVisitor.this, context);
                UID c = null;
                if (expr.getContext() != null){
                    c = (UID) expr.getContext().accept(QueryRDFVisitor.this, context);
                }
                return new TransformIterator<STMT, Bindings>(connection.findStatements(s, p, o, c, false), transformer);
            }
        };
    }

    private Transformer<Bindings, STMT> createStatementTransformer(final PatternBlock expr){
        if (expr.getContext() != null){
            return new Transformer<Bindings, STMT>(){
                @Override
                public STMT transform(Bindings input) {
                    return new STMT(
                            (ID)expr.getSubject().accept(QueryRDFVisitor.this, input),
                            (UID)expr.getPredicate().accept(QueryRDFVisitor.this, input),
                            (NODE)expr.getObject().accept(QueryRDFVisitor.this, input),
                            (UID)expr.getContext().accept(QueryRDFVisitor.this, input)
                    );
                }
            };    
        }else{
            return new Transformer<Bindings, STMT>(){
                @Override
                public STMT transform(Bindings input) {
                    return new STMT(
                            (ID)expr.getSubject().accept(QueryRDFVisitor.this, input),
                            (UID)expr.getPredicate().accept(QueryRDFVisitor.this, input),
                            (NODE)expr.getObject().accept(QueryRDFVisitor.this, input)
                    );
                }
            };            
        }
        
    }

    private Transformer<STMT, Bindings> createBindingsTransformer(PatternBlock expr, final Bindings bindings){
        final String s = getKey(expr.getSubject());
        final String p = getKey(expr.getPredicate());
        final String o = getKey(expr.getObject());
        final String c = getKey(expr.getContext());
        
        return new Transformer<STMT, Bindings>(){
            @Override
            public Bindings transform(STMT input) {
                bindings.clear();
                bind(bindings, s, input.getSubject());
                bind(bindings, p, input.getPredicate());
                bind(bindings, o, input.getObject());
                bind(bindings, c, input.getContext());
                return bindings;
            }
        };
    }

    private void bind(Bindings bindings, String key, NODE value) {
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
    public NODE visit(Constant<?> expr, Bindings context) {
        return (NODE) expr.getConstant();
    }

    @Override
    public Object visit(FactoryExpression<?> expr, Bindings context) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate visit(final Operation<?> expr, final Bindings context) {
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
                    boolean rv =  context.get(key) != null;
                    return op == Ops.IS_NOT_NULL ? rv : !rv;
                }
            };

        }else if (op == Ops.IS_NOT_NULL){
            final String key = getKey(expr.getArg(0));
            return new Predicate(){
                @Override
                public boolean evaluate(Object object) {
                    return context.get(key) != null;
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
    public NODE visit(ParamExpression<?> expr, Bindings context) {
        return context.get(expr.getName());
    }

    @Override
    public NODE visit(Path<?> expr, Bindings context) {
        return context.get(expr.getMetadata().getExpression().toString());
    }

    @Override
    public Object visit(SubQueryExpression<?> expr, Bindings context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(TemplateExpression<?> expr, Bindings context) {
        throw new UnsupportedOperationException();
    }

}
