/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import javax.annotation.Nullable;

import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.Constant;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.EConstructor;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.path.Path;
import com.mysema.query.types.path.PathMetadata;
import com.mysema.query.types.path.PathType;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.*;
import com.mysema.rdfbean.query.AbstractProjectingQuery;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.sesame.SesameDialect;


/**
 * SesameQuery provides a query implementation for Sesame Repository
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameQuery extends 
    AbstractProjectingQuery<SesameQuery, Value, Resource, BNode, URI, Literal, Statement>  implements Closeable{
   
    private static final Logger logger = LoggerFactory.getLogger(SesameQuery.class);
    
    private static final Logger queryTreeLogger = LoggerFactory.getLogger("com.mysema.rdfbean.sesame.queryTree");
    
    static{
        SesameQueryHolder.init();
    }
    
    private final StatementPattern.Scope patternScope;
    
    private ValueExpr filterConditions;

    private boolean idPropertyInOperation = false;
    
    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();

    private final Map<Path<?>, Var> pathToVar = new HashMap<Path<?>, Var>();
    
    private final Map<Path<?>, Var> pathToMatchedVar = new HashMap<Path<?>,Var>();

    private final ProjectionElemList projection = new ProjectionElemList();
    
    private final List<ExtensionElem> extensions = new ArrayList<ExtensionElem>();
    
    private final List<OrderElem> orderElements = new ArrayList<OrderElem>(); 

    private TupleResult queryResult;

    private final Map<UID, Var> resToVar = new HashMap<UID, Var>();
    
    private final Map<Object,Var> constToVar = new HashMap<Object,Var>();
    
    private final VarNameIterator varNames = new VarNameIterator("_var");
    
    private final VarNameIterator extNames = new VarNameIterator("_ext");
    
    private final JoinBuilder joinBuilder;
    
    private final boolean includeInferred = true;
    
    private final RepositoryConnection connection;
    
    private final Configuration conf;
    
    private final boolean datatypeInference;
        
    public SesameQuery(Session session, 
            SesameDialect dialect, 
            RepositoryConnection connection, 
            StatementPattern.Scope patternScope,
            boolean datatypeInference) {
        super(dialect, session);
        this.connection = Assert.notNull(connection, "connection was null");
        this.conf = session.getConfiguration();
        this.datatypeInference = datatypeInference;
        this.patternScope = patternScope;
        this.joinBuilder = new JoinBuilder(dialect, datatypeInference);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected SesameQuery addToProjection(Expr<?>... o) {
        for (Expr<?> expr : o) {
            if (expr instanceof Path){
                projection.addElement(new ProjectionElem(transformPath((Path<?>) expr).getName()));    
            }else if (expr instanceof EConstructor){    
                EConstructor<?> constructor = (EConstructor<?>)expr;
                for (Expr<?> arg : constructor.getArgs()){
                    addToProjection(arg);
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
        return this;
    }
    
    @Override
    public SesameQuery from(Expr<?>... o) {
        for (Expr<?> expr : o) {
            handleRootPath((Path<?>) expr);
        }
        return this;
    }
    
        
    @Override
    public SesameQuery where(EBoolean... o) {
        for (int i = 0; i < o.length; i++) {
            addFilterCondition(toValue(o[i]));
        }
        return this;
    }
    
    @Override
    public SesameQuery orderBy(OrderSpecifier<?>... o) {
        for (OrderSpecifier<?> os : o){
            orderElements.add(new OrderElem(toValue(os.getTarget()), os.isAscending()));
        }
        return this;
    }
        
    public void close() throws IOException {
        if (queryResult != null){
            try {
                queryResult.close();
            } catch (StoreException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
        
    protected <RT> RT convert(Class<RT> rt, Literal literal) {
        return conf.getConverterRegistry().fromString(literal.getLabel(), rt);
    }

    private void addFilterCondition(ValueExpr filterCondition) {
        if (filterConditions == null) {
            filterConditions = filterCondition;
        } else if (filterCondition != null){
            filterConditions = new And(filterConditions, filterCondition);
        }
    }
    
    private SesameQuery match(Var sub, UID pred, Var obj) {
        joinBuilder.add(createPattern(sub, pred, obj), inOptionalPath());
        return this;
    }
    
    private SesameQuery match(JoinBuilder builder, Var sub, UID pred, Var obj){
        builder.add(createPattern(sub, pred, obj), false);
        return this;
    }
    
    private StatementPattern createPattern(Var s, UID p, Var o){
        return new StatementPattern(patternScope, 
                Assert.notNull(s, "subject is null"), 
                toVar(Assert.notNull(p, "predicate is null")), 
                Assert.notNull(o, "object is null"));
    }
        
    @Override
    protected Iterator<Value[]> getInnerResults() {
        // from 
        TupleExpr tupleExpr = joinBuilder.getJoins();        
        // where
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
        
        // evaluate it
        try {
            TupleQueryModel query;
            if (getMetadata().isDistinct()){
                query = new TupleQueryModel(new Distinct(tupleExpr));
            }else{
                query = new TupleQueryModel(tupleExpr);
            }
            
            logQuery(query);
            
            // TODO : replace the following two lines with proper Sesame integration
            SesameQueryHolder.set(query);
            TupleQuery tupleQuery = connection.prepareTupleQuery(SesameQueryHolder.QUERYDSL, "");                       
            tupleQuery.setIncludeInferred(includeInferred);
            
            queryResult =  tupleQuery.evaluate();
            return new Iterator<Value[]>(){
                public boolean hasNext() {
                    try {                        
                        return queryResult.hasNext();
                    } catch (StoreException e) {
                        throw new RuntimeException(e.getMessage(), e);
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
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
                public void remove() {
                    // do nothing
                }                
            };
        } catch (StoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }        
    }

    protected void logQuery(TupleQueryModel query) {
        if (queryTreeLogger.isDebugEnabled()){
            queryTreeLogger.debug(query.toString());                    
        }                        
        if (logger.isDebugEnabled()){
            logger.debug(new QuerySerializer(query,false).toString());
        }
    }

    @SuppressWarnings("unchecked")
    private ID getResourceForLID(Expr<?> arg) {
        String lid = ((Constant<String>)arg).getConstant();
        ID id = conf.getIdentityService().getID(new LID(lid));
        return id;
    }

    private void handleRootPath(Path<?> path) {
//        getMetadata().addFrom(path.asExpr());
        Var var = new Var(path.getMetadata().getExpression().toString());
        varNames.disallow(var.getName());
        pathToVar.put(path, var);
        UID rdfType = MappedClass.getMappedClass(path.getType()).getUID();
        if (rdfType != null){
            match(var, RDF.type, toVar(rdfType));
        } else {
            throw new IllegalArgumentException("No types mapped against " + path.getType().getName());
        }
    }
    
    private boolean inOptionalPath(){
        return operatorStack.contains(Ops.IS_NULL) || operatorStack.contains(Ops.OR);
    }
    
    private boolean inNegation(){
        return operatorStack.contains(Ops.NOT);
    }
  
    private Var toVar(UID id) {
        if (resToVar.containsKey(id)) {
            return resToVar.get(id);
        } else {
            Var var = new Var(varNames.next(), dialect.getResource(id));
            var.setAnonymous(true);
            resToVar.put(id, var);
            return var;
        }
    }
    
    private Var toVar(Value value){
        if (constToVar.containsKey(value)){
            return constToVar.get(value);
        }else{
            Var var = new Var(varNames.next(), value);
            var.setAnonymous(true);
            constToVar.put(value, var);
            return var;
        }
    }
            
    @SuppressWarnings("unchecked")
    @Nullable
    private ValueExpr toValue(Expr<?> expr) {
        if (expr instanceof BooleanBuilder){
            return toValue(((BooleanBuilder)expr).getValue());
            
        }else if (expr instanceof Path) {
            return transformPath((Path<?>) expr);
            
        } else if (expr instanceof Operation) {
            Operation<?,?> op = (Operation<?,?>)expr;
            operatorStack.push(op.getOperator());            
            ValueExpr rv =  transformOperation(op);
            operatorStack.pop();
            return rv;
            
        } else if (expr instanceof Constant) {
            return transformConstant((Constant<?>)expr);
            
        } else {
            throw new IllegalArgumentException("Unsupported expr instance : " + expr + " (" + expr.getClass().getSimpleName()+ ")");
        }
    }
    
    @SuppressWarnings("unchecked")
    private Var transformConstant(Constant<?> constant) {
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
                rdfValue = dialect.getLiteral(label, dialect.getURI(datatype));
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
    
    @SuppressWarnings("unchecked")
    @Nullable
    private ValueExpr transformOperation(Operation<?,?> operation) {
        Operator<?> op = operation.getOperator();
        Transformer transformer;
        
        if (op.equals(Ops.IN)){
            // path in path
            if (operation.getArg(0) instanceof Path && operation.getArg(1) instanceof Path){
                // TODO : make path in path work for RDF sequences and containers
                Path<?> path = (Path<?>) operation.getArg(0);
                if (path.getMetadata().getParent() == null && !inNegation()){
                    Path<?> otherPath = (Path<?>) operation.getArg(1);
                    pathToMatchedVar.put(otherPath, transformPath(path));
                    transformPath(otherPath);
                    return null;
                }else{
                    transformer = SesameTransformers.getTransformer(Ops.EQ_OBJECT);    
                }
                
            // const in path    
            }else if (operation.getArg(0) instanceof Constant && operation.getArg(1) instanceof Path){
                // TODO : make const in path work for RDF sequences and containers
                if (!inNegation()){
                    Var var = transformConstant((Constant<?>) operation.getArg(0));
                    Var path = transformPath((Path<?>) operation.getArg(1));
                    path.setValue(var.getValue());
                    return null;    
                }else{
                    transformer = SesameTransformers.getTransformer(Ops.EQ_OBJECT);
                }
                
            // path in collection    
            }else if (operation.getArg(0) instanceof Path && operation.getArg(1) instanceof Constant){
                Expr<Object> expr = (Expr<Object>)operation.getArg(0);
                Constant<?> constant = (Constant<?>)operation.getArg(1);
                Collection<?> collection = (Collection<?>)constant.getConstant();
                BooleanBuilder bo = new BooleanBuilder();
                for (Object elem : collection){
                    bo.or(expr.eq(elem));
                }               
                return toValue(bo);
                
            }else{
                throw new IllegalArgumentException("Unsupported operation " + operation);
            }
            
         // size(col)    
        }else if (isSizeCompareConstant(operation, op)){            
            return transformSizeCompareConstant(operation, op);
            
        }else if (op.equals(Ops.COL_IS_EMPTY)){
            Var pathVar = transformPath((Path<?>)operation.getArg(0));            
            if (inNegation()){
                return new Compare(pathVar, toVar(RDF.nil), Compare.CompareOp.EQ);    
            }else{
                pathVar.setValue(dialect.getResource(RDF.nil));
                return null;    
            }
            
        }else if (op.equals(Ops.MAP_ISEMPTY)){
            transformer = SesameTransformers.getTransformer(Ops.IS_NULL);
            
        // containsKey / containsValue
        }else if (op.equals(Ops.CONTAINS_KEY) || op.equals(Ops.CONTAINS_VALUE)){
            Path<?> path = (Path<?>) operation.getArg(0);
            Var pathVar = transformPath(path);
            MappedPath mappedPath = getMappedPathForPropertyPath(path); 
            if (!mappedPath.getMappedProperty().isLocalized()){
                Var valNode, keyNode;
                if (op.equals(Ops.CONTAINS_KEY)){
                    keyNode = (Var) toValue(operation.getArg(1));
                    valNode = null;                        
                }else{                    
                    keyNode = null;
                    valNode = (Var) toValue(operation.getArg(1));
                }                
                return transformMapAccess(pathVar, mappedPath, valNode, keyNode);
            }else{  
                // TODO
                return null;
            }
                        
        // path == path
        }else if (isPathEqPath(operation, op)){
            Path<?> path = (Path<?>) operation.getArg(0);
            if (path.getMetadata().getParent() == null && !inNegation()){
                Path<?> otherPath = (Path<?>) operation.getArg(1);
                pathToMatchedVar.put(otherPath, transformPath(path));
                transformPath(otherPath);
                return null;
            }else{
                transformer = SesameTransformers.getTransformer(Ops.EQ_OBJECT);    
            }
            
        // path == const OR path != const
        }else if (isPathEqNeConstant(operation, op)){
            return transformPathEqNeConstant(operation);
            
        // expr typeOf expr
        }else if (op.equals(Ops.INSTANCE_OF)){    
            StatementPattern pattern = new StatementPattern(
                    patternScope,
                    (Var)toValue(operation.getArg(0)),
                    toVar(RDF.type),
                    (Var)toValue(operation.getArg(1)));
            if (inNegation()){
                return new Exists(pattern);    
            }else{
                joinBuilder.add(pattern, true);
                return null;
            }                        
            
        }else{
            transformer = SesameTransformers.getTransformer(op);
        }       
        
        // handle operation via transformer
        if (transformer != null) {            
            List<ValueExpr> values = new ArrayList<ValueExpr>(operation.getArgs().size());
            for (Expr<?> arg : operation.getArgs()) {
                ValueExpr value;
                // transform LID strings to Resources
                if (idPropertyInOperation 
                        && op.equals(Ops.NE_OBJECT) 
                        && arg instanceof Constant){
                    ID id = getResourceForLID(arg);
                    value = toVar(dialect.getResource(id));
                }else{
                    value = toValue(arg);    
                }
                
                if (op != Ops.AND && op != Ops.OR){
                    Assert.notNull(value, arg + " resolved to null");
                }
                values.add(value);
            }    
            return transformer.transform(values);
        } else {
            throw new IllegalArgumentException("Unsupported operation instance : " + operation);
        }
    }

    @Nullable
    private ValueExpr transformMapAccess(Var pathVar, MappedPath mappedPath, 
            @Nullable Var valNode, @Nullable Var keyNode) {
        MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
        JoinBuilder builder = new JoinBuilder((SesameDialect)dialect, datatypeInference);
        if (valNode != null){
            if (mappedProperty.getValuePredicate() != null){
                match(builder, pathVar, mappedProperty.getValuePredicate(), valNode);
            }else if (!inNegation()){    
                pathVar.setValue(valNode.getValue());
            }
        }
        if (keyNode != null){
            match(builder, pathVar, mappedProperty.getKeyPredicate(), keyNode);   
        }                        
        
        if (!builder.isEmpty()){
            return new Exists(builder.getJoins()); 
        }else if (inNegation()){
            return new Compare(pathVar, valNode, CompareOp.EQ);
        }else{
            return null;
        }        
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private ValueExpr transformPathEqNeConstant(Operation<?, ?> operation) {
        Path<?> path = (Path<?>) operation.getArg(0);
        Var pathVar = transformPath(path);
        Value constValue;
        
        MappedPath mappedPath;
        PathType pathType = path.getMetadata().getPathType();
        if (pathType.equals(PathType.PROPERTY)) {
            mappedPath = getMappedPathForPropertyPath(path);   
        }else{
            mappedPath = getMappedPathForPropertyPath(path.getMetadata().getParent());
        }
        Locale locale = null;
        if (!mappedPath.getPredicatePath().isEmpty()){
            if (mappedPath.getMappedProperty().isLocalized()){
                String value = operation.getArg(1).toString();
                if (pathType.equals(PathType.PROPERTY)){
                    locale = session.getCurrentLocale();
                }else if (pathType.equals(PathType.MAPVALUE_CONSTANT)){
                    locale = ((Constant<Locale>)path.getMetadata().getExpression()).getConstant();                        
                }else{
                    throw new IllegalArgumentException("Unsupported path type " + pathType);
                }
                constValue = dialect.getLiteral(value, locale);
                
            }else{
                constValue = ((Var) toValue(operation.getArg(1))).getValue();    
            }                                
        }else{
            ID id = getResourceForLID(operation.getArg(1));
            constValue = dialect.getResource(id);
        }
        
        if (Ops.equalsOps.contains(operation.getOperator())){
            if (!inOptionalPath()){
                pathVar.setValue(constValue);
                return null;    
            }else{
                return new Compare(pathVar, toVar(constValue), Compare.CompareOp.EQ);
            }
                            
        }else{
            Var constVar = toVar(constValue);
            Compare compare = new Compare(pathVar, constVar, Compare.CompareOp.NE);
            if (locale != null){
                Var langVar = toVar(dialect.getLiteral(LocaleUtil.toLang(locale)));
                return new And(
                    compare, 
                    new Compare(new Lang(pathVar), langVar, Compare.CompareOp.EQ));
            }else{
                return compare;
            }
        }                
    }
        
    private ValueExpr transformSizeCompareConstant(Operation<?, ?> operation, Operator<?> op) {        
        @SuppressWarnings("unchecked")
        int size = getIntValue((Constant<Integer>) operation.getArg(1));
        if (op == Ops.GOE){
            op = Ops.GT;
            size--;
        }else if (op == Ops.LOE){
            op = Ops.LT;
            size++;
        }
        
        JoinBuilder builder = new JoinBuilder((SesameDialect)dialect, datatypeInference);
        // path from size operation
        Path<?> path = (Path<?>)((Operation<?,?>)operation.getArg(0)).getArg(0); 
        Var pathVar = transformPath(path);                                
        for (int i=0; i < size-1; i++){
            Var rest = new Var(varNames.next());
            match(builder, pathVar, RDF.rest, rest);
            pathVar = rest;
        }
        
        // last
        if (op == Ops.EQ_PRIMITIVE){
            match(builder, pathVar, RDF.rest, toVar(RDF.nil));
            
        }else if (op == Ops.GT){
            Var next = new Var(varNames.next());
            match(builder, pathVar, RDF.rest, next);
            match(builder, next, RDF.rest, new Var(varNames.next()));
            
        }else if (op == Ops.LT){
            match(builder, pathVar, RDF.rest, new Var(varNames.next()));
        }          
        
        if (op != Ops.LT){
            return new Exists(builder.getJoins());    
        }else{
            return new Not(new Exists(builder.getJoins()));
        }           
        
    }

    @SuppressWarnings("unchecked")
    private boolean isPathEqPath(Operation<?, ?> operation, Operator<?> op) {
        return  Ops.equalsOps.contains(op)
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Path;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isPathEqNeConstant(Operation<?, ?> operation, Operator<?> op) {
        return (Ops.equalsOps.contains(op) || Ops.notEqualsOps.contains(op)) 
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Constant;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isSizeCompareConstant(Operation<?, ?> operation, Operator<?> op) {
        if (Ops.compareOps.contains(op)){
            Expr<?> arg1 = operation.getArg(0);
            return (arg1 instanceof Operation && ((Operation)arg1).getOperator().equals(Ops.COL_SIZE));
        }else{
            return false;
        }        
    }
    
    private Var transformPath(Path<?> path) {
        if (pathToVar.containsKey(path)) {
            return pathToVar.get(path);
            
        } else if (path.getMetadata().getParent() != null) {
            PathMetadata<?> md = path.getMetadata();
            PathType pathType = md.getPathType();            
            Var parentNode = transformPath(md.getParent());
            Var pathNode = null;
            Var matchedVar = pathToMatchedVar.get(path);
            
            if (pathType.equals(PathType.PROPERTY)) {
                MappedPath mappedPath = getMappedPathForPropertyPath(path); 
                List<MappedPredicate> predPath = mappedPath.getPredicatePath();
                if (predPath.size() > 0){
                    for (int i = 0; i < predPath.size(); i++){
                        MappedPredicate pred = predPath.get(i);
                        if (matchedVar != null && i == predPath.size() -1){
                            pathNode = matchedVar;
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
                    idPropertyInOperation = true;
                    // id property
                    pathNode =  parentNode;
                }

            } else if (pathType.equals(PathType.ARRAYVALUE) || pathType.equals(PathType.LISTVALUE)) {
                // ?!? 
                throw new UnsupportedOperationException(pathType + " not supported!");
                
            } else if (pathType.equals(PathType.ARRAYVALUE_CONSTANT) || pathType.equals(PathType.LISTVALUE_CONSTANT)) {
                @SuppressWarnings("unchecked")
                int index = getIntValue((Constant<Integer>)md.getExpression());                
                for (int i = 0; i < index; i++){
                    pathNode = new Var(varNames.next());
                    match(parentNode, RDF.rest, pathNode);
                    parentNode = pathNode;
                }
                pathNode = new Var(varNames.next());
                match(parentNode, RDF.first, pathNode);

            } else if (pathType.equals(PathType.MAPVALUE) || pathType.equals(PathType.MAPVALUE_CONSTANT)) {     
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

        } else {
            throw new IllegalArgumentException("Undeclared path " + path);
        }

    }
    
}
