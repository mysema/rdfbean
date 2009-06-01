/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.EConstant;
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
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.ConverterRegistry;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.query.AbstractProjectingQuery;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.sesame.SesameDialect;
import com.mysema.rdfbean.sesame.query.SesameOps.Transformer;
import com.mysema.rdfbean.sesame.query.serializer.QuerySerializer;


/**
 * SesameQuery provides a query implementation for Sesame Sail
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameQuery extends 
    AbstractProjectingQuery<SesameQuery, Value, Resource, BNode, URI, Literal, Statement>  implements Closeable{
   
    private static final Logger logger = LoggerFactory.getLogger(SesameQuery.class);
    
    private static final Logger queryTreeLogger = LoggerFactory.getLogger("com.mysema.rdfbean.sesame.queryTree");
    
    private final StatementPattern.Scope patternScope;
    
    static{
        SesameQueryHolder.init();
    }

    private ValueExpr filterConditions;

    private boolean idPropertyInOperation = false;
    
    private Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();

    private final Map<Path<?>, Var> pathToVar = new HashMap<Path<?>, Var>();
    
    private final Map<Path<?>, Var> pathToMatchedVar = new HashMap<Path<?>,Var>();

    private ProjectionElemList projection = new ProjectionElemList();
    
    private List<OrderElem> orderElements = new ArrayList<OrderElem>(); 

    private TupleResult queryResult;

    private final Map<UID, Var> resToVar = new HashMap<UID, Var>();
    
    private final Map<Object,Var> constToVar = new HashMap<Object,Var>();
    
    private VarNameIterator varNames = new VarNameIterator("v");
    
    private final SesameOps sailOps = SesameOps.DEFAULT;
    
    private JoinBuilder statementPatterns = new JoinBuilder();
    
    private boolean includeInferred = true;
    
    private RepositoryConnection connection;
    
    private Configuration conf;
        
    public SesameQuery(Session session, SesameDialect dialect, RepositoryConnection connection, StatementPattern.Scope patternScope) {
        super(dialect, session);
        this.connection = connection;
        conf = session.getConfiguration();
        this.patternScope = patternScope;
    }
    
//    public SesameQuery(SesameConnection session) {
//        this(session, StatementPattern.Scope.DEFAULT_CONTEXTS);    
//    }  

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
                throw new IllegalArgumentException("Unsupported projection element " + expr);
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
        session.close();
        if (queryResult != null){
            try {
                queryResult.close();
            } catch (StoreException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
        
    protected <RT> RT convert(Class<RT> rt, Literal literal) {
        return conf.getConverterRegistry().fromString(literal.getLabel(), null, rt);
    }

    private void addFilterCondition(ValueExpr filterCondition) {
        if (filterConditions == null) {
            filterConditions = filterCondition;
        } else if (filterCondition != null){
            filterConditions = new And(filterConditions, filterCondition);
        }
    }
    
    private SesameQuery match(Var sub, UID pred, Var obj) {
        StatementPattern pattern = new StatementPattern(patternScope, sub, toVar(pred), obj);
        if (inOptionalPath()){
            statementPatterns.leftJoin(pattern);
        }else{
            statementPatterns.join(pattern);
        }    
        return this;
    }
    
    private SesameQuery match(JoinBuilder builder, Var sub, UID pred, Var obj){
        StatementPattern pattern = new StatementPattern(patternScope, sub, toVar(pred), obj);
        builder.join(pattern);
        return this;
    }
        
    @Override
    protected Iterator<Value[]> getInnerResults() {
        // from 
        TupleExpr tupleExpr = statementPatterns.getJoins();        
        // where
        if (filterConditions != null){
            tupleExpr = new Filter(tupleExpr, filterConditions);
        }        
        // order
        if(!orderElements.isEmpty()){
            tupleExpr = new Order(tupleExpr, orderElements); 
        }        
        // projection
        tupleExpr = new Projection(tupleExpr, projection);
        
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
        String lid = ((EConstant<String>)arg).getConstant();
        ID id = conf.getIdentityService().getID(new LID(lid));
        return id;
    }

    private void handleRootPath(Path<?> path) {
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
        return operatorStack.contains(Ops.ISNULL);
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
        
    private ValueExpr toValue(Expr<?> expr) {
        if (expr instanceof Path) {
            return transformPath((Path<?>) expr);
            
        } else if (expr instanceof Operation) {
            Operation<?,?> op = (Operation<?,?>)expr;
            operatorStack.push(op.getOperator());            
            ValueExpr rv =  transformOperation(op);
            operatorStack.pop();
            return rv;
            
        } else if (expr instanceof EConstant) {
            return transformConstant((EConstant<?>)expr);
            
        } else {
            throw new IllegalArgumentException("Unsupported expr instance : " + expr);
        }
    }
    
    private Var transformConstant(EConstant<?> constant) {
        Object javaValue = constant.getConstant();
        if (constToVar.containsKey(javaValue)){
            return constToVar.get(javaValue);
        }else{
            Value rdfValue;
            ConverterRegistry converter = conf.getConverterRegistry();
            if (javaValue instanceof Class){
                rdfValue = getTypeForJavaClass((Class<?>)javaValue);
            }else if (converter.supports(javaValue.getClass())){
                String label = converter.toString(javaValue);
                UID datatype = converter.getDatatype(javaValue);
                rdfValue = dialect.getLiteral(label, dialect.getURI(datatype));
            }else{
                ID id = session.getId(javaValue);
                rdfValue = id != null ? dialect.getResource(id) : null;
            }        
            Var var = new Var(varNames.next(), rdfValue);
            var.setAnonymous(true);
            constToVar.put(javaValue, var);
            return var;
        }        
    }
    
    private ValueExpr transformOperation(Operation<?,?> operation) {
        Operator<?> op = operation.getOperator();
        Transformer transformer;
        
        // path in path
        if (isPathInPath(operation, op)){
            // TODO : make path in path work for RDF sequences
            Path<?> path = (Path<?>) operation.getArg(0);
            if (path.getMetadata().getParent() == null){
                Path<?> otherPath = (Path<?>) operation.getArg(1);
                pathToMatchedVar.put(otherPath, transformPath(path));
                transformPath(otherPath);
                return null;
            }else{
                transformer = sailOps.getTransformer(Ops.EQ_OBJECT);    
            }
            
         // size(col)    
        }else if (isSizeCompareConstant(operation, op)){            
            return transformSizeCompareConstant(operation, op);
            
        // is empty / not is empty
        }else if (op.equals(Ops.COL_ISEMPTY) || op.equals(Ops.COL_ISNOTEMPTY)){
            Var pathVar = transformPath((Path<?>)operation.getArg(0));            
            if (op.equals(Ops.COL_ISEMPTY)){
                if (inNegation()){
                    return new Compare(pathVar, toVar(RDF.nil), Compare.CompareOp.EQ);    
                }else{
                    pathVar.setValue(dialect.getResource(RDF.nil));
                    return null;    
                }
            }else{
                Var rest = new Var(varNames.next());
                JoinBuilder builder = new JoinBuilder();
                match(builder, pathVar, RDF.rest, rest);
                return new Exists(builder.getJoins());
            }
            
        }else if (op.equals(Ops.MAP_ISEMPTY) || op.equals(Ops.MAP_ISNOTEMPTY)){
            // TODO
            return null;
            
        // path == path
        }else if (isPathEqPath(operation, op)){
            Path<?> path = (Path<?>) operation.getArg(0);
            if (path.getMetadata().getParent() == null){
                Path<?> otherPath = (Path<?>) operation.getArg(1);
                pathToMatchedVar.put(otherPath, transformPath(path));
                transformPath(otherPath);
                return null;
            }else{
                transformer = sailOps.getTransformer(Ops.EQ_OBJECT);    
            }
            
        // path == const OR path != const
        }else if (isPathEqNeConstant(operation, op)){
            return transformPathEqNeConstant(operation);
            
        // expr typeOf expr
        }else if (op.equals(Ops.ISTYPEOF)){    
            StatementPattern pattern = new StatementPattern(
                    patternScope,
                    (Var)toValue(operation.getArg(0)),
                    toVar(RDF.type),
                    (Var)toValue(operation.getArg(1)));
            if (inNegation()){
                return new Exists(pattern);    
            }else{
                statementPatterns.join(pattern);
                return null;
            }                        
            
        }else{
            transformer = sailOps.getTransformer(op);
        }       
        
        // handle operation via transformer
        if (transformer != null) {            
            List<ValueExpr> values = new ArrayList<ValueExpr>(operation.getArgs().size());
            for (Expr<?> arg : operation.getArgs()) {
                // transform LID strings to Resources
                if (idPropertyInOperation 
                        && op.equals(Ops.NE_OBJECT) 
                        && arg instanceof EConstant){
                    ID id = getResourceForLID(arg);
                    values.add(toVar(dialect.getResource(id)));
                }else{
                    values.add(toValue(arg));    
                }
            }    
            return transformer.transform(values);
        } else {
            throw new IllegalArgumentException("Unsupported operation instance : " + operation);
        }
    }

    @SuppressWarnings("unchecked")
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
                    locale = ((EConstant<Locale>)path.getMetadata().getExpression()).getConstant();                        
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
            pathVar.setValue(constValue);
            return null;    
            
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
        int size = getIntValue((EConstant<Integer>) operation.getArg(1));
        if (op == Ops.GOE){
            op = Ops.GT;
            size--;
        }else if (op == Ops.LOE){
            op = Ops.LT;
            size++;
        }
        
        JoinBuilder builder = new JoinBuilder();
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

    private boolean isPathInPath(Operation<?, ?> operation, Operator<?> op) {
        return op.equals(Ops.IN) 
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Path;
    }

    private boolean isPathEqPath(Operation<?, ?> operation, Operator<?> op) {
        return  Ops.equalsOps.contains(op)
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Path;
    }
    
    private boolean isPathEqNeConstant(Operation<?, ?> operation, Operator<?> op) {
        return (Ops.equalsOps.contains(op) || Ops.notEqualsOps.contains(op)) 
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof EConstant;
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
                            match(parentNode, pred.uid(), pathNode);
                        } else {
                            match(pathNode, pred.uid(), parentNode);
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
                int index = getIntValue((EConstant<Integer>)md.getExpression());                
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
