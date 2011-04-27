package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.collections15.IteratorUtils;
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
import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.*;
import com.mysema.util.FilterIterable;
import com.mysema.util.IterableChain;
import com.mysema.util.LimitingIterable;
import com.mysema.util.PairIterator;


/**
 * @author tiwe
 *
 */
public class QueryRDFVisitor implements RDFVisitor<Object, Bindings>{

    private static final Map<String, Pattern> patterns = new HashMap<String, Pattern>();

    private static final Map<String, Pattern> caseInsensitivePatterns = new HashMap<String, Pattern>();

    private static final NODEComparator nodeComparator = new NODEComparator();

    private final RDFConnection connection;
    
    @Nullable
    private Expression<UID> context;

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

    private Transformer<STMT, Bindings> createBindingsTransformer(PatternBlock expr, @Nullable Expression<UID> context, final Bindings bindings){
        final String s = getKey(expr.getSubject());
        final String p = getKey(expr.getPredicate());
        final String o = getKey(expr.getObject());
        final String c = getKey(expr.getContext() != null ? expr.getContext() : context);

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
                    return op == Ops.LT || op == Ops.LOE || op == Ops.BEFORE || op == Ops.BOE;
                }else if (rv == 0){
                    return op == Ops.LOE || op == Ops.GOE || op == Ops.BOE || op == Ops.AOE;
                }else{
                    return op == Ops.GT || op == Ops.GOE || op == Ops.AFTER || op == Ops.AOE;
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

    private Predicate<Bindings> createLikePredicate(final Operation<?> expr){

        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE) expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                NODE rhs = (NODE) expr.getArg(1).accept(QueryRDFVisitor.this, bindings);
                if (lhs != null && rhs != null){
                    Pattern pattern;
                    Map<String, Pattern> cache = patterns;
                    pattern = cache.get(rhs.getValue());
                    if (pattern == null){
                        String regex = rhs.getValue().replace("%", ".*").replaceAll("_", ".");
                        pattern = Pattern.compile(regex);
                        cache.put(rhs.getValue(), pattern);
                    }
                    return pattern.matcher(lhs.getValue()).matches();
                }else{
                    return false;
                }
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
                    Pattern pattern;
                    Map<String, Pattern> cache = op == Ops.MATCHES ? patterns : caseInsensitivePatterns;
                    pattern = cache.get(rhs.getValue());
                    if (pattern == null){
                        pattern = Pattern.compile(rhs.getValue(), op == Ops.MATCHES ? 0 : Pattern.CASE_INSENSITIVE);
                        cache.put(rhs.getValue(), pattern);
                    }
                    return pattern.matcher(lhs.getValue()).matches();
                }else{
                    return false;
                }
            }
        };
    }

    private GraphQuery createGraphQuery(QueryMetadata md, final Iterable<Bindings> iterable) {
        List<PatternBlock> patternBlocks = new ArrayList<PatternBlock>();
        for (Expression<?> e : md.getProjection()){
            if (e instanceof PatternBlock){
                patternBlocks.add((PatternBlock)e);
            }else if (e instanceof ContainerBlock){
                for (Block b : ((ContainerBlock)e).getBlocks()){
                    patternBlocks.add((PatternBlock)b);
                }
            }
        }
        
        final List<Transformer<Bindings, STMT>> transformers = new ArrayList<Transformer<Bindings, STMT>>(patternBlocks.size());
        for (PatternBlock pb : patternBlocks){
            transformers.add(createStatementTransformer(pb));
        }
        
        return new GraphQuery(){
            @SuppressWarnings("unchecked")
            @Override
            public CloseableIterator<STMT> getTriples() {
                List<Iterator<STMT>> iterators = new ArrayList<Iterator<STMT>>(transformers.size());
                for (Transformer<Bindings, STMT> transformer : transformers){
                    iterators.add(new TransformIterator<Bindings, STMT>(iterable.iterator(), transformer));
                }
                return new IteratorAdapter<STMT>(IteratorUtils.chainedIterator((Collection)iterators));
            }
        };
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

        final Transformer<Bindings, Map<String,NODE>> bindingsToMap = new Transformer<Bindings, Map<String,NODE>>(){
            @Override
            public Map<String, NODE> transform(Bindings input) {
                if (variables.isEmpty()){
                    return input.toMap();
                }else{
                    return input.toMap(variables);
                }
            }
        };

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

    @Nullable
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
            iterable = iterables.get(0);
            for (int i = 1; i < iterables.size(); i++){
                final Iterable<Bindings> pr = iterable, next = iterables.get(i);
                iterable = new Iterable<Bindings>(){
                    @Override
                    public Iterator<Bindings> iterator() {
                        return new PairIterator<Bindings>(pr, next);
                    }
                };
            }
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
        try{
            context = expr.getContext();
            return visit((ContainerBlock)expr, bindings);    
        }finally{
            context = null;
        }        
    }

    @Override
    public Pair<Iterable<Bindings>, Bindings> visit(GroupBlock expr, Bindings bindings) {
        return visit((ContainerBlock)expr, bindings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(final Operation<?> expr, Bindings bindings) {
        final Operator<?> op = expr.getOperator();
        if (op == Ops.EQ_OBJECT || op == Ops.EQ_PRIMITIVE){
            return createEqPredicate(expr);

        }else if (op == Ops.NE_OBJECT || op == Ops.NE_PRIMITIVE){
            return createNePredicate(expr);

        }else if (op == Ops.AND){
            return createAndPredicate(expr, bindings);
            
        }else if (op == Ops.IN){
            // expand IN to OR/EQ
            BooleanBuilder builder = new BooleanBuilder();
            for (Object o : ((Constant<Collection>)expr.getArg(1)).getConstant()) {
                builder.or(ExpressionUtils.eqConst((Expression)expr.getArg(0), o));
            }
            return builder.getValue().accept(this, bindings);
            
        }else if (op == Ops.OR){
            return createOrPredicate(expr, bindings);

        }else if (op == Ops.NOT){
            return new NotPredicate<Bindings>( (Predicate) expr.getArg(0).accept(this, bindings));

        }else if (op == Ops.IS_NULL || op == Ops.IS_NOT_NULL){
            return createBoundPredicate(expr, op);

        }else if (op == Ops.LT || op == Ops.GT || op == Ops.LOE || op == Ops.GOE
               || op == Ops.BEFORE || op == Ops.AFTER || op == Ops.BOE || op == Ops.AOE){
            return createComparePredicate(expr, op);

        }else if (op == Ops.MATCHES || op == Ops.MATCHES_IC){
            return createMatchesPredicate(expr, op);

        }else if (op == Ops.STARTS_WITH || op == Ops.ENDS_WITH || op == Ops.STRING_CONTAINS
               || op == Ops.STARTS_WITH_IC || op == Ops.ENDS_WITH_IC || op == Ops.STRING_CONTAINS_IC){
            return createStringMatchPredicate(expr, op);

        }else if (op == Ops.LIKE){
            return createLikePredicate(expr);

        }else if (op == Ops.EQ_IGNORE_CASE){
            return createEqIgnoreCasePredicate(expr);

        }else if (op == Ops.STRING_IS_EMPTY){
            return createStringIsEmptyPredicate(expr);

        }else if (op == Ops.CONCAT){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            NODE rhs = (NODE) expr.getArg(1).accept(this, bindings);
            return new LIT(lhs.getValue()+rhs.getValue());

        }else if (op == Ops.LOWER){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            return new LIT(lhs.getValue().toLowerCase());

        }else if (op == Ops.UPPER){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            return new LIT(lhs.getValue().toUpperCase());

        }else if (op == Ops.TRIM){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            return new LIT(lhs.getValue().trim());

        }else if (op == Ops.SUBSTR_1ARG){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            NODE rhs = (NODE) expr.getArg(1).accept(this, bindings);
            return new LIT(lhs.getValue().substring(Integer.parseInt(rhs.getValue())));

        }else if (op == Ops.SUBSTR_2ARGS){
            NODE arg0 = (NODE) expr.getArg(0).accept(this, bindings);
            NODE arg1 = (NODE) expr.getArg(1).accept(this, bindings);
            NODE arg2 = (NODE) expr.getArg(2).accept(this, bindings);
            return new LIT(arg0.getValue().substring(
                    Integer.parseInt(arg1.getValue()),
                    Integer.parseInt(arg2.getValue())));

        }else if (op == Ops.CHAR_AT){
            NODE lhs = (NODE) expr.getArg(0).accept(this, bindings);
            NODE rhs = (NODE) expr.getArg(1).accept(this, bindings);
            return new LIT(String.valueOf(lhs.getValue().charAt(Integer.parseInt(rhs.getValue()))));

        }else{
            throw new IllegalArgumentException(expr.toString());
        }
    }

    private Predicate<Bindings> createStringMatchPredicate(final Operation<?> expr, final Operator<?> op) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE) expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                NODE rhs = (NODE) expr.getArg(1).accept(QueryRDFVisitor.this, bindings);
                if (lhs == null || rhs == null){
                    return false;
                }else if (op == Ops.STARTS_WITH){
                    return lhs.getValue().startsWith(rhs.getValue());
                }else if (op == Ops.STARTS_WITH_IC){
                    return lhs.getValue().toLowerCase().startsWith(rhs.getValue().toLowerCase());
                }else if (op == Ops.ENDS_WITH){
                    return lhs.getValue().endsWith(rhs.getValue());
                }else if (op == Ops.ENDS_WITH_IC){
                    return lhs.getValue().toLowerCase().endsWith(rhs.getValue().toLowerCase());
                }else if (op == Ops.STRING_CONTAINS){
                    return lhs.getValue().contains(rhs.getValue());
                }else if (op == Ops.STRING_CONTAINS_IC){
                    return lhs.getValue().toLowerCase().contains(rhs.getValue().toLowerCase());
                }else{
                    throw new IllegalArgumentException(op.toString());
                }
            }
        };
    }

    private Predicate<Bindings> createEqIgnoreCasePredicate(final Operation<?> expr) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE) expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                NODE rhs = (NODE) expr.getArg(1).accept(QueryRDFVisitor.this, bindings);
                if (lhs != null && rhs != null){
                    return lhs.getValue().equalsIgnoreCase(rhs.getValue());
                }else{
                    return lhs == rhs;
                }

            }
        };
    }

    private Predicate<Bindings> createStringIsEmptyPredicate(final Operation<?> expr) {
        return new Predicate<Bindings>(){
            @Override
            public boolean evaluate(Bindings bindings) {
                NODE lhs = (NODE) expr.getArg(0).accept(QueryRDFVisitor.this, bindings);
                return lhs != null ? lhs.getValue().isEmpty() : false;
            }
        };
    }

    @Override
    public Pair<Iterable<Bindings>, Bindings> visit(OptionalBlock expr, final Bindings bindings) {
        // FIXME
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
        final Transformer<STMT, Bindings> transformer = createBindingsTransformer(expr, context, bindings);
        final Expression<UID> _context = context;
        Iterable<Bindings> iterable = new Iterable<Bindings>(){
            @Override
            public Iterator<Bindings> iterator() {
                Bindings parent = bindings.getParent();
                ID s = (ID) expr.getSubject().accept(QueryRDFVisitor.this, parent);
                UID p = (UID) expr.getPredicate().accept(QueryRDFVisitor.this, parent);
                NODE o = (NODE) expr.getObject().accept(QueryRDFVisitor.this, parent);
                UID c = null;
                if (expr.getContext() != null){
                    c = (UID) expr.getContext().accept(QueryRDFVisitor.this, parent);
                }else if (_context != null){
                    c = (UID)_context.accept(QueryRDFVisitor.this, parent);
                }
                bindings.clear();
                return new TransformIterator<STMT, Bindings>(connection.findStatements(s, p, o, c, false), transformer);
            }

            @Override
            public String toString(){
                return expr.toString();
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

        com.mysema.query.types.Predicate where = md.getWhere();
        List<Constant<UID>> uids = new ArrayList<Constant<UID>>();
        for (JoinExpression je : md.getJoins()) {
            uids.add((Constant<UID>) je.getTarget());
        }
        if (uids.size() == 1) {
            where = Blocks.graph(uids.get(0), (Block)where);
        } else  if (uids.size() > 1) {
            QUID g = new QUID("__g"); // TODO : use constant
            BooleanBuilder b = new BooleanBuilder();
            for (Constant<UID> uid : uids) {
                b.or(g.eq(uid));
            }
            where = Blocks.graphFilter(g, (Block)where, b.getValue());
        }

        Bindings whereBindings = new Bindings(initialBindings);
        Iterable<Bindings> iterable = (Iterable<Bindings>) ((Pair)where.accept(this, whereBindings)).getFirst();

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
            throw new IllegalArgumentException(queryType.toString());
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
