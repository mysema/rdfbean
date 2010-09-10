/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import org.apache.commons.collections15.Transformer;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.types.*;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.Inference;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.query.AbstractProjectingQuery;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * SesameQuery provides a query implementation for Sesame Repository
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameQuery 
    extends AbstractProjectingQuery<SesameQuery, Value, Resource, BNode, URI, Literal, Statement>  
    implements TransformerContext, BeanQuery, Closeable{
   
    private static final boolean includeInferred = true;
    
    private static final Logger logger = LoggerFactory.getLogger(SesameQuery.class);
   
    private static final Logger queryTreeLogger = LoggerFactory.getLogger("com.mysema.rdfbean.sesame.queryTree");
    
    private static final URI RDF_TYPE = org.openrdf.model.vocabulary.RDF.TYPE;
    
    private static final Map<Operator<?>,OperationTransformer> transformers = new HashMap<Operator<?>,OperationTransformer>();
    
    static{
        register(new FunctionTransformer());
        register(new DelegateTransformer());
        
        register(new CoalesceTransformer());
        register(new BetweenTransformer());
        register(new BooleanTransformer());
        register(new CastTransformer());
        register(new ColIsEmptyTransformer());
        register(new CompareTransformer());
        register(new ContainsKeyValueTransformer());
        register(new EqualsTransformer());
        register(new ExistsTransformer());
        register(new InstanceOfTransformer());
        register(new InTransformer());
        register(new IsNullTransformer());
        register(new MapIsEmptyTransformer());
        register(new MathTransformer());
        register(new RegexTransformer());
        register(new StringContainsTransformer());
        register(new OrdinalTransformer());
    }
    
    private Map<Path<?>,Var> allPaths = new HashMap<Path<?>,Var>();
    
    private final Configuration conf;

    private final RepositoryConnection connection;
    
    private final Map<Object,Var> constToVar = new HashMap<Object,Var>();
    
    private final VarNameIterator extNames = new VarNameIterator("_ext_");
    
    @Nullable
    private ValueExpr filterConditions;
    
    private final Inference inference;
    
    private JoinBuilder joinBuilder; 

    private final Ontology<UID> ontology;

    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();
    
    private Map<Path<?>, Var> pathToKnownVar = new HashMap<Path<?>,Var>();
    
    private Map<Path<?>, Var> pathToVar = new HashMap<Path<?>, Var>();
    
    private final StatementPattern.Scope patternScope;
    
    private TupleResult queryResult;
    
    private final Map<UID, Var> resToVar = new HashMap<UID, Var>();
    
    private final Transformer<StatementPattern,TupleExpr> stmtTransformer = new Transformer<StatementPattern,TupleExpr>(){

        @Override
        public TupleExpr transform(StatementPattern pattern) {
            if (inference.untypedAsString()){
                Var object = pattern.getObjectVar();
                if (object.getValue() instanceof Literal){
                    return transformLiteralPattern(pattern, object);                    
                }            
            }        
            
            if (inference.subClassOf()){
                Value predicate = pattern.getPredicateVar().getValue();
                Var object = pattern.getObjectVar();
                if (predicate != null && predicate.equals(RDF_TYPE) && object.getValue() != null){
                    return transformTypePattern(pattern, object);
                }
            }
            
            return pattern;
        }
        
    };
    
    private final ValueFactory valueFactory;
    
    private final VarNameIterator varNames = new VarNameIterator("_var_");
    
    public SesameQuery(
            Session session, 
            Dialect<Value, Resource, BNode, URI, Literal, Statement> dialect,
            ValueFactory valueFactory,
            RepositoryConnection connection, 
            StatementPattern.Scope patternScope,
            Ontology<UID> ontology,
            Inference inference) {
        super(dialect, session);        
        this.connection = Assert.notNull(connection,"connection");
        this.conf = session.getConfiguration();
        this.ontology = Assert.notNull(ontology,"ontology");
        this.inference = Assert.notNull(inference,"inference");
        this.patternScope = patternScope;
        this.valueFactory = valueFactory;
        this.joinBuilder = new JoinBuilder(stmtTransformer);
    }
    

    private static void register(OperationTransformer transformer){
        for (Operator<?> operator : transformer.getSupportedOperations()){
            transformers.put(operator, transformer);
        }
    }
        
    private void addFilterCondition(@Nullable ValueExpr filterCondition) {
        if (filterConditions == null) {
            filterConditions = filterCondition;
        } else if (filterCondition != null){
            filterConditions = new And(filterConditions, filterCondition);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addProjection(Expr<?> expr, ProjectionElemList projection, List<ExtensionElem> extensions){
        if (expr instanceof Path){
            projection.addElement(new ProjectionElem(toVar((Path<?>) expr).getName()));    
        }else if (expr instanceof EConstructor){    
            EConstructor<?> constructor = (EConstructor<?>)expr;
            for (Expr<?> arg : constructor.getArgs()){
                addProjection(arg, projection, extensions);
            }
        }else{
            ValueExpr val = toValue(expr);
            if (val instanceof Var){
                projection.addElement(new ProjectionElem(((Var)val).getName()));
            }else{
                String extLabel = extNames.next();
                projection.addElement(new ProjectionElem(extLabel));
                extensions.add(new ExtensionElem(val, extLabel));
            }
        }  
    }
    
    public void close() throws IOException {
        if (queryResult != null){
            try {
                queryResult.close();
            } catch (StoreException e) {
                throw new RepositoryException(e.getMessage(), e);
            }
        }
    }

    protected <RT> RT convert(Class<RT> rt, Literal literal) {
        return conf.getConverterRegistry().fromString(literal.getLabel(), rt);
    }
    
    @Override
    public long count() {
        // TODO : use aggregate function
        long total = 0l;
        Iterator<?> it = getInnerResults();
        while (it.hasNext()){
            total++;
            it.next();
        }
        return total;
    }
    
    @Override
    public JoinBuilder createJoinBuilder() {
        return new JoinBuilder(stmtTransformer);
    }
        
    private StatementPattern createPattern(Var s, UID p, Var o){
        return new StatementPattern(patternScope, 
                Assert.notNull(s, "subject is null"), 
                toVar(Assert.notNull(p, "predicate is null")), 
                Assert.notNull(o, "object is null"));
    }

    private TupleExpr createTupleExpr(TupleExpr from, 
            ValueExpr filterConditions, 
            List<OrderElem> orderElements,
            List<ExtensionElem> extensions,
            ProjectionElemList projection,
            QueryModifiers modifiers){
        
        TupleExpr tupleExpr = from;
        
        if (filterConditions != null){
            tupleExpr = new Filter(tupleExpr, filterConditions);
        }        
        // order
        if(!orderElements.isEmpty()){
            tupleExpr = new Order(tupleExpr, orderElements); 
        }        
        // projection
        if (!extensions.isEmpty()){
            tupleExpr = new Extension(tupleExpr, extensions);
        }        
        
        if (!projection.getElements().isEmpty()){
            tupleExpr = new Projection(tupleExpr, projection);    
        }                

        // limit / offset
        if (modifiers.isRestricting()){
            Long limit = modifiers.getLimit();
            Long offset = modifiers.getOffset();            
            tupleExpr = new Slice(tupleExpr, 
                    offset != null ? offset.intValue() : 0,
                    limit != null ? limit.intValue() : -1);
        }
        
        return tupleExpr;
    }
    
    @Override
    public Var createVar() {
       return new Var(varNames.next());
    }
    
    @Override
    protected Iterator<Value[]> getInnerResults() {
        QueryMetadata metadata = queryMixin.getMetadata();
        List<OrderElem> orderElements = new ArrayList<OrderElem>();
        ProjectionElemList projection = new ProjectionElemList();
        List<ExtensionElem> extensions = new ArrayList<ExtensionElem>();
        
        // from
        for (JoinExpression join : metadata.getJoins()){            
            handleRootPath((Path<?>) join.getTarget());
        }        
        // where
        if (metadata.getWhere() != null){
            addFilterCondition(toValue(metadata.getWhere()));
        }        
        // order by (optional paths)
        joinBuilder.setOptional();
        for (OrderSpecifier<?> os : metadata.getOrderBy()){
            orderElements.add(new OrderElem(toValue(os.getTarget()), os.isAscending()));
        }        
        
        allPaths.putAll(pathToVar);
        pathToVar = allPaths;
        // select (optional paths)
        for (Expr<?> expr : metadata.getProjection()){
            addProjection(expr, projection, extensions);
        }
        joinBuilder.setMandatory();
        
        TupleExpr tupleExpr = createTupleExpr(
                joinBuilder.getTupleExpr(),   // from
                filterConditions,             // where
                orderElements,                // order
                extensions,                   // select
                projection,
                getMetadata().getModifiers());// paging
        
        
        // evaluate it
        try {
            TupleQueryModel query;
            if (getMetadata().isDistinct()){
                query = new TupleQueryModel(new Distinct(tupleExpr));
            }else{
                query = new TupleQueryModel(tupleExpr);
            }
            
            logQuery(query);            
            queryResult = DirectQuery.query(connection, query, includeInferred);
            
            return new Iterator<Value[]>(){
                public boolean hasNext() {
                    try {                        
                        return queryResult.hasNext();
                    } catch (StoreException e) {
                        throw new RepositoryException(e.getMessage(), e);
                    }
                }
                public Value[] next() {
                    try {
                        BindingSet bindingSet = queryResult.next();
                        List<String> bindingNames = queryResult.getBindingNames();
                        Value[] values = new Value[bindingNames.size()];
                        for (int i = 0; i < bindingNames.size(); i++){
                            values[i] = bindingSet.getValue(bindingNames.get(i));
                        }
                        return values;
                    } catch (StoreException e) {
                        throw new RepositoryException(e.getMessage(), e);
                    }
                }
                public void remove() {
                    // do nothing
                }                
            };
        } catch (StoreException e) {
            throw new RepositoryException(e.getMessage(), e);
        }        
    }
    
    @Override
    public Locale getLocale() {
        return session.getCurrentLocale();
    }
    

    @Override
    public MappedClass getMappedClass(Class<?> javaClass) {
        return conf.getMappedClass(javaClass);
    }
    
    @Override
    public MappedPath getMappedPath(Path<?> path) {
        Assert.notNull(path, "path");
        return this.getMappedPathForPropertyPath(path);
    }
    
    @Override
    public Scope getPatternScope() {
        return patternScope;
    }
        
    public ID getResourceForLID(String arg) {
        return session.getId(new LID(arg));
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    private void handleRootPath(Path<?> path) {
        Var var = new Var(path.getMetadata().getExpression().toString());
        varNames.disallow(var.getName());
        pathToVar.put(path, var);
        MappedClass mappedClass = conf.getMappedClass(path.getType());
        UID rdfType = mappedClass.getUID();
        if (rdfType != null){
            match(var, RDF.type, toVar(rdfType));
        } else {
            throw new IllegalArgumentException("No types mapped against " + path.getType().getName());
        }
    }

    public boolean inNegation(){
        int notIndex = operatorStack.lastIndexOf(Ops.NOT);
        if (notIndex > -1){
            int existsIndex = operatorStack.lastIndexOf(Ops.EXISTS);
            return notIndex > existsIndex;
        }
        return false;
    }
    
    public boolean inOptionalPath(){
        return operatorStack.contains(Ops.IS_NULL) || 
            (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    @Override
    public boolean isKnown(Path<?> path) {
        return pathToVar.containsKey(path);
    }
  
    protected void logQuery(TupleQueryModel query) {
        if (queryTreeLogger.isDebugEnabled()){
            queryTreeLogger.debug(query.toString());                    
        }                        
        if (logger.isDebugEnabled()){
            logger.debug(new QuerySerializer(query,false).toString());
        }
    }
            
    public void match(JoinBuilder builder, Var sub, UID pred, Var obj){
        builder.add(createPattern(sub, pred, obj));
    }
    
    public void match(Var sub, UID pred, Var obj) {
        joinBuilder.add(createPattern(sub, pred, obj));
    }
    
    @Override
    public void register(Path<?> path, Var var) {
        pathToKnownVar.put(path, var);
    }

    private StatementPattern replaceObject(StatementPattern pattern, Var obj){
        return new StatementPattern(pattern.getScope(), 
                pattern.getSubjectVar(), 
                pattern.getPredicateVar(),
                obj,
                pattern.getContextVar());
    }
        
    public TupleExpr toTuples(SubQueryExpression<?> subQuery){
        EBoolean where = subQuery.getMetadata().getWhere();
        
        Map<Path<?>,Var> normalPathToVar = pathToVar;        
        pathToVar = new HashMap<Path<?>,Var>(pathToVar);
        Map<Path<?>,Var> normalPathToKnownVar = pathToKnownVar;
        pathToKnownVar = new HashMap<Path<?>,Var>(pathToKnownVar);
        JoinBuilder normalJoins = joinBuilder;
        joinBuilder = createJoinBuilder();
        for (JoinExpression join : subQuery.getMetadata().getJoins()){            
            handleRootPath((Path<?>) join.getTarget());
        }
       
        // list
        ProjectionElemList projection = new ProjectionElemList();            
        List<ExtensionElem> extensions = new ArrayList<ExtensionElem>();
        for (Expr<?> expr : subQuery.getMetadata().getProjection()){
            addProjection(expr, projection, extensions);
        }        
        
        ValueExpr filters = toValue(where);
        
        // from
        TupleExpr tupleExpr = createTupleExpr(
                joinBuilder.getTupleExpr(),             // from
                filters,                                // where
                Collections.<OrderElem>emptyList(),     // order
                extensions,                             // select
                projection,
                subQuery.getMetadata().getModifiers()); // paging
                
        joinBuilder = normalJoins;
        pathToVar = normalPathToVar;
        pathToKnownVar = normalPathToKnownVar;
        return tupleExpr;
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public ValueExpr toValue(Expr<?> expr) {
        if (expr instanceof Path) {
            return toVar((Path)expr);  
        } else if (expr instanceof Operation) {
            return  toValue((Operation<?>)expr);            
        } else if (expr instanceof Constant) {
            return toVar((Constant<?>)expr);            
        } else {
            Expr<?> extracted = expr.accept(ExtractorVisitor.DEFAULT, null);
            if (extracted != expr){
                return toValue(extracted);
            }else{
                throw new IllegalArgumentException(expr.toString());
            }
        }
    }

    @Override
    public Value toValue(ID id) {
        return dialect.getResource(id);
    }

    @Nullable
    private ValueExpr toValue(Operation<?> operation) {
        boolean outerOptional = inOptionalPath();
        boolean innerOptional = false;
        Map<Path<?>,Var> origPathToVar = null;
        Map<Path<?>,Var> origPathToKnownVar = null;
        operatorStack.push(operation.getOperator());        
        if (!outerOptional && inOptionalPath()){
            joinBuilder.setOptional();
            innerOptional = true;
            
            origPathToVar = pathToVar;
            origPathToKnownVar = pathToKnownVar;
            pathToVar = new HashMap<Path<?>,Var>(origPathToVar);
            pathToKnownVar = new HashMap<Path<?>,Var>(origPathToKnownVar);
        }                  
        try{
            OperationTransformer transformer = transformers.get(operation.getOperator());
            if (transformer != null) {             
                return transformer.transform(operation, this);
            } else {
                throw new IllegalArgumentException(operation.toString());
            }    
        }finally{
            operatorStack.pop();
            if (!outerOptional && innerOptional){
                joinBuilder.setMandatory();
                
                allPaths.putAll(pathToVar);
                pathToVar = origPathToVar;
                pathToKnownVar = origPathToKnownVar;
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    public Var toVar(Constant<?> constant) {
        Object javaValue = constant.getConstant();
        if (constToVar.containsKey(javaValue)){
            return constToVar.get(javaValue);
        }else{
            Value rdfValue;
            ConverterRegistry converter = conf.getConverterRegistry();
            if (javaValue instanceof Class){
                UID datatype = converter.getDatatype((Class<?>)javaValue);
                if (datatype != null){
                    return toVar(datatype);
                }else{
                    rdfValue = getTypeForDomainClass((Class<?>)javaValue);
                }
            }else if (converter.supports(javaValue.getClass())){
                String label = converter.toString(javaValue);
                UID datatype = converter.getDatatype(javaValue.getClass());
                rdfValue = valueFactory.createLiteral(label, dialect.getURI(datatype));
            }else{
                ID id = session.getId(javaValue);                
                rdfValue =  dialect.getResource(Assert.notNull(id, "id is null"));
            }        
            Var var = new Var(varNames.next(), rdfValue);
            var.setAnonymous(true);
            constToVar.put(javaValue, var);
            return var;
        }        
    }

    public Var toVar(Path<?> path) {
        if (pathToVar.containsKey(path)) {
            return pathToVar.get(path);
            
        } else if (path.getMetadata().getParent() != null) {
            PathMetadata<?> md = path.getMetadata();
            PathType pathType = md.getPathType();            
            Var parentNode = toVar(md.getParent());
            Var pathNode = null;
            Var matchedVar = pathToKnownVar.get(path);
            
            if (pathType.equals(PathType.PROPERTY)) {
                MappedPath mappedPath = getMappedPathForPropertyPath(path); 
                List<MappedPredicate> predPath = mappedPath.getPredicatePath();
                if (predPath.size() > 0){
                    for (int i = 0; i < predPath.size(); i++){
                        MappedPredicate pred = predPath.get(i);
                        if (matchedVar != null && (i == predPath.size() -1)){
                            pathNode = matchedVar;
                        }else if (i == predPath.size() - 1){    
                            pathNode = new Var(path.toString().replace('.', '_'));
                            varNames.disallow(pathNode.getName());
                        }else{
                            pathNode = new Var(varNames.next());    
                        }                        
                        if (!pred.inv()) {
                            match(parentNode, pred.getUID(), pathNode);
                        } else {
                            match(pathNode, pred.getUID(), parentNode);
                        }
                        parentNode = pathNode;
                    }
                     
                }else{
                    pathNode =  parentNode;
                }

            } else if (pathType.equals(PathType.ARRAYVALUE) 
                    || pathType.equals(PathType.LISTVALUE)) { 
                throw new UnsupportedOperationException(pathType + " not supported!");
                
            // array value & list value    
            } else if (pathType.equals(PathType.ARRAYVALUE_CONSTANT) 
                    || pathType.equals(PathType.LISTVALUE_CONSTANT)) {
                @SuppressWarnings("unchecked")
                int index = getIntValue((Constant<Integer>)md.getExpression());                
                for (int i = 0; i < index; i++){
                    pathNode = new Var(varNames.next());
                    match(parentNode, RDF.rest, pathNode);
                    parentNode = pathNode;
                }
                pathNode = new Var(varNames.next());
                match(parentNode, RDF.first, pathNode);

            // map value    
            } else if (pathType.equals(PathType.MAPVALUE) 
                    || pathType.equals(PathType.MAPVALUE_CONSTANT)) {     
                MappedPath mappedPath = getMappedPathForPropertyPath(md.getParent()); 
                MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                if (!mappedProperty.isLocalized()){
                    match(parentNode, mappedProperty.getKeyPredicate(), 
                        ((Var)toValue(path.getMetadata().getExpression())));
                        
                    if (mappedProperty.getValuePredicate() != null){
                        pathNode = new Var(varNames.next());
                        match(parentNode, mappedProperty.getValuePredicate(), pathNode);
                    }else{
                        pathNode = parentNode;
                    }
                }else{  
                    pathNode = parentNode;            
                }
                
 
            } else {
                throw new UnsupportedOperationException(pathType + " not supported!");

            }
            pathToVar.put(path, pathNode);
            return pathNode;

        }else if (path.getMetadata().getPathType().equals(PathType.DELEGATE)){
            PathMetadata<?> md = path.getMetadata();
            ValueExpr rv = toValue(md.getExpression());
            if (rv instanceof Var){
                pathToVar.put(path, (Var)rv);
                return (Var)rv;
            }else{
                throw new IllegalArgumentException(md.getExpression() + " can't be converted into a Var");
            }            
            
        } else {
            throw new IllegalArgumentException("Undeclared path " + path);
        }
    }

    public Var toVar(UID id) {
        if (resToVar.containsKey(id)) {
            return resToVar.get(id);
        } else {
            Var var = new Var(varNames.next(), dialect.getResource(id));
            var.setAnonymous(true);
            resToVar.put(id, var);
            return var;
        }
    }

    public Var toVar(Value value){
        if (constToVar.containsKey(value)){
            return constToVar.get(value);
        }else{
            Var var = new Var(varNames.next(), value);
            var.setAnonymous(true);
            constToVar.put(value, var);
            return var;
        }
    }

    private TupleExpr transformLiteralPattern(StatementPattern pattern, Var object) {
        Literal lit = (Literal) pattern.getObjectVar().getValue();
        if (lit.getDatatype() != null && lit.getDatatype().equals(XMLSchema.STRING)){
            // match untyped literal for xsd:string vars
            Var obj2 = new Var(object.getName()+"_untyped", valueFactory.createLiteral(lit.getLabel()));
            StatementPattern pattern2 = replaceObject(pattern, obj2);
            return new Union(pattern, pattern2);
        }else{
            return pattern;
        }
    }

    private TupleExpr transformTypePattern(StatementPattern pattern, Var object) {
        if (object.getValue() instanceof URI){
            Collection<UID> subtypes = ontology.getSubtypes(dialect.getUID((URI) object.getValue()));
            if (subtypes.size() > 1){
                List<StatementPattern> patterns = new ArrayList<StatementPattern>(subtypes.size());
                int counter = 1;
                for (UID type : subtypes){
                    Var subtypeVar = new Var(object.getName() + "_" + (counter++));
                    subtypeVar.setValue(dialect.getURI(type));
                    patterns.add(replaceObject(pattern, subtypeVar));
                }
                return new Union(patterns);
            }else{
                return pattern;
            }
            
        }else{
            return pattern;
        }        
    }



}
