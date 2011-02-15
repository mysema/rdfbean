package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.AndPredicate;
import org.apache.commons.collections15.functors.AnyPredicate;
import org.apache.commons.collections15.functors.NotPredicate;
import org.apache.commons.collections15.iterators.TransformIterator;
import org.apache.commons.lang.ObjectUtils;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.commons.lang.Pair;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.*;
import com.mysema.util.FilterIterable;
import com.mysema.util.IterableChain;
import com.mysema.util.LimitingIterable;
import com.mysema.util.MultiIterator;


/**
 * @author tiwe
 *
 */
public class QueryRDFVisitor implements RDFVisitor<Object, Bindings>{

    private static final Transformer<Bindings, Map<String,NODE>> bindingsToMap = new Transformer<Bindings, Map<String,NODE>>(){
        @Override
        public Map<String, NODE> transform(Bindings input) {
//            System.err.println(input.toString()+" matched\n");
            return input.toMap();
        }        
    };

    private static final NODEComparator nodeComparator = new NODEComparator();
    
    private final RDFConnection connection;
    
    public QueryRDFVisitor(RDFConnection connection) {
        this.connection = connection;
    }

    private void bind(Bindings bindings, String key, NODE value) {
        if (key != null && bindings.get(key) == null){            
            bindings.put(key, value);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Predicate<Bindings> createAndPredicate(final Operation<?> expr, Bindings bindings) {
        return new AndPredicate<Bindings>(
                (Predicate)expr.getArg(0).accept(this, bindings),
                (Predicate)expr.getArg(1).accept(this, bindings));
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
//                System.err.println(bindings);
                return bindings;
            }
        };
    }

    private BooleanQuery createBooleanQuery(final Iterable<Bindings> iterable) {
        return new BooleanQuery(){
            @Override
            public boolean getBoolean() {
                return iterable.iterator().hasNext();
            }
        };
    }

    private Predicate<Bindings> createBoundPredicate(final Operation<?> expr, final Operator<?> op) {
        final String key = getKey(expr.getArg(0));
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                boolean rv =  bindings.get(key) != null;
                return op == Ops.IS_NOT_NULL ? rv : !rv;
            }
        };
    }

    private Predicate<Bindings> createComparePredicate(final Operation<?> expr, final Operator<?> op) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE)expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                NODE rhs = (NODE)expr.getArg(1).accept(QueryRDFVisitor.this, bindings);
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
    }

    private Predicate<Bindings> createEqPredicate(final Operation<?> expr) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                return ObjectUtils.equals(
                        expr.getArg(0).accept(QueryRDFVisitor.this, bindings),
                        expr.getArg(1).accept(QueryRDFVisitor.this, bindings));
            }
        };
    }
    

    private Predicate<Bindings> createMatchesPredicate(final Operation<?> expr, final Operator<?> op) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE) expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                NODE rhs = (NODE) expr.getArg(1).accept(QueryRDFVisitor.this, bindings); 
                if (lhs != null && rhs != null){
                    // TODO : cache patterns
                    Pattern pattern = Pattern.compile(rhs.getValue(), op == Ops.MATCHES ? 0 : Pattern.CASE_INSENSITIVE);
                    return pattern.matcher(lhs.getValue()).matches();                    
                }else{
                    return false;
                }
            }
        };   
    }

    private GraphQuery createGraphQuery(QueryMetadata md, final Iterable<Bindings> iterable) {
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
    }

    private Predicate<Bindings> createNePredicate(final Operation<?> expr) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {              
                return !ObjectUtils.equals(
                        expr.getArg(0).accept(QueryRDFVisitor.this, bindings),
                        expr.getArg(1).accept(QueryRDFVisitor.this, bindings));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Predicate<Bindings> createOrPredicate(final Operation<?> expr, Bindings bindings) {
        return new AnyPredicate<Bindings>( new Predicate[]{
                (Predicate)expr.getArg(0).accept(this, bindings),
                (Predicate)expr.getArg(1).accept(this, bindings)});
    }

    private Transformer<Bindings, STMT> createQuadTransformer(final PatternBlock expr) {
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
    }

    private Transformer<Bindings, STMT> createStatementTransformer(final PatternBlock expr){
        if (expr.getContext() != null){
            return createQuadTransformer(expr);    
        }else{
            return createTripleTransformer(expr);            
        }        
    }

    private Transformer<Bindings, STMT> createTripleTransformer(final PatternBlock expr) {
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

    private TupleQuery createTupleQuery(QueryMetadata md, final Iterable<Bindings> iterable) {
        final List<String> variables = new ArrayList<String>(md.getProjection().size());
        for (Expression<?> expr : md.getProjection()){
            String key = getKey(expr);
            variables.add(key != null ? key : expr.toString());
        }
        return new TupleQuery(){
            @Override
            public CloseableIterator<Map<String, NODE>> getTuples() {
                Iterator<Map<String,NODE>> it = new TransformIterator<Bindings, Map<String,NODE>>(iterable.iterator(), bindingsToMap);
                return new IteratorAdapter<Map<String, NODE>>(it);
            }
            @Override
            public List<String> getVariables() {
                return variables;
            }
        };
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
    public NODE visit(Constant<?> expr, Bindings bindings) {
        return (NODE) expr.getConstant();
    }

    @SuppressWarnings("unchecked")
    private Pair<Iterable<Bindings>, Bindings> visit(ContainerBlock expr, Bindings bindings){
        final List<Iterable<Bindings>> iterables = new ArrayList<Iterable<Bindings>>(expr.getBlocks().size());
        Bindings previous = null;
        for (Block block : expr.getBlocks()){
            Bindings input = previous != null ? new Bindings(previous) : bindings; 
            Pair<Iterable<Bindings>, Bindings> iterableAndBindings = (Pair) block.accept(this, input);
            iterables.add(iterableAndBindings.getFirst());
            previous = iterableAndBindings.getSecond();
        }
        
        // merge
        Iterable<Bindings> iterable;        
        if (iterables.size() == 1){
            iterable = iterables.get(0);
        }else{
            iterable = new Iterable<Bindings>(){
                @Override
                public Iterator<Bindings> iterator() {
                    return new MultiIterator<Bindings>(iterables);
                }                
            };
        }

        // filter
        if (expr.getFilters() != null){
            Predicate<Bindings> predicate = (Predicate) expr.getFilters().accept(this, previous);
            iterable = new FilterIterable<Bindings>(iterable, predicate);
        }
        
        return Pair.of(iterable, bindings);
    }

    @Override
    public Object visit(FactoryExpression<?> expr, Bindings bindings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pair<Iterable<Bindings>, Bindings> visit(GraphBlock expr, Bindings bindings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pair<Iterable<Bindings>, Bindings> visit(GroupBlock expr, Bindings bindings) {
        return visit((ContainerBlock)expr, bindings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate visit(final Operation<?> expr, Bindings bindings) {
        final Operator<?> op = expr.getOperator();
        if (op == Ops.EQ_OBJECT || op == Ops.EQ_PRIMITIVE){
            return createEqPredicate(expr);
            
        }else if (op == Ops.NE_OBJECT || op == Ops.NE_PRIMITIVE){
            return createNePredicate(expr);
            
        }else if (op == Ops.AND){
            return createAndPredicate(expr, bindings);

        }else if (op == Ops.OR){
            return createOrPredicate(expr, bindings);

        }else if (op == Ops.NOT){
            return new NotPredicate( (Predicate) expr.getArg(0).accept(this, bindings));

        }else if (op == Ops.IS_NULL || op == Ops.IS_NOT_NULL){
            return createBoundPredicate(expr, op);

        }else if (op == Ops.LT || op == Ops.GT || op == Ops.LOE || op == Ops.GOE){
            return createComparePredicate(expr, op);

        }else if (op == Ops.MATCHES || op == Ops.MATCHES_IC){    
            return createMatchesPredicate(expr, op);
            
        }else{
            throw new IllegalArgumentException(expr.toString());
        }
    }

    @Override
    public Pair<Iterable<Bindings>, Bindings> visit(OptionalBlock expr, final Bindings bindings) {
        final Pair<Iterable<Bindings>, Bindings> pair =  visit((ContainerBlock)expr, bindings);
        Iterable<Bindings> iterable = new Iterable<Bindings>(){
            @Override
            public Iterator<Bindings> iterator() {
                Iterator<Bindings> iterator = pair.getFirst().iterator();
                if (iterator.hasNext()){
                    return iterator;
                }else{
                    pair.getSecond().clear();
                    return Collections.singleton(bindings).iterator();
                }
            }
            
        };
        return Pair.of(iterable, pair.getSecond());
        
    }

    @Override
    public NODE visit(ParamExpression<?> expr, Bindings bindings) {
        return bindings.get(expr.getName());
    }

    @Override
    public NODE visit(Path<?> expr, Bindings bindings) {
        return bindings.get(expr.getMetadata().getExpression().toString());
    }

    @Override
    public Pair<Iterable<Bindings>,Bindings> visit(final PatternBlock expr, final Bindings bindings) {
        final Transformer<STMT, Bindings> transformer = createBindingsTransformer(expr, bindings);
        Iterable<Bindings> iterable = new Iterable<Bindings>(){
            @Override
            public Iterator<Bindings> iterator() {
                ID s = (ID) expr.getSubject().accept(QueryRDFVisitor.this, bindings);
                UID p = (UID) expr.getPredicate().accept(QueryRDFVisitor.this, bindings);
                NODE o = (NODE) expr.getObject().accept(QueryRDFVisitor.this, bindings);
                UID c = null;
                if (expr.getContext() != null){
                    c = (UID) expr.getContext().accept(QueryRDFVisitor.this, bindings);
                }
                bindings.clear();
                return new TransformIterator<STMT, Bindings>(connection.findStatements(s, p, o, c, false), transformer);
            }
        };
        return Pair.of(iterable, bindings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        Bindings initialBindings = new Bindings();
        for (Map.Entry<ParamExpression<?>, Object> entry : md.getParams().entrySet()){
            initialBindings.put(entry.getKey().getName(), (NODE) entry.getValue());
        }      
        
        Bindings whereBindings = new Bindings(initialBindings);
        Iterable<Bindings> iterable = (Iterable<Bindings>) ((Pair)md.getWhere().accept(this, whereBindings)).getFirst();

        // TODO : sort
        
        // paging
        if (md.getModifiers().isRestricting()){
            iterable = new LimitingIterable<Bindings>(iterable, md.getModifiers());
        }
                
        if (queryType == QueryLanguage.GRAPH){
            return createGraphQuery(md, iterable);

        }else if (queryType == QueryLanguage.TUPLE){
            return createTupleQuery(md, iterable);

        }else if (queryType == QueryLanguage.BOOLEAN){
            return createBooleanQuery(iterable);

        }else{
            return null;
        }
    }


    @Override
    public Object visit(SubQueryExpression<?> expr, Bindings bindings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(TemplateExpression<?> expr, Bindings bindings) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<? extends Iterable<Bindings>, Bindings> visit(UnionBlock expr, Bindings bindings) {
        // TODO : make sure this works
        List<Iterable<Bindings>> iterables = new ArrayList<Iterable<Bindings>>();
        for (Block block : expr.getBlocks()){
            Pair<Iterable<Bindings>, Bindings> iterableAndBindings = (Pair)block.accept(this, bindings);
            iterables.add(iterableAndBindings.getFirst());
        }
        return Pair.of(new IterableChain<Bindings>(iterables), bindings);
    }

}
