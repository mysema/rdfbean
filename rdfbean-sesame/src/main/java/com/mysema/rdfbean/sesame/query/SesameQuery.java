/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.grammar.types.PathMetadata.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.*;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.query.grammar.Ops;
import com.mysema.query.grammar.OrderSpecifier;
import com.mysema.query.grammar.Ops.Op;
import com.mysema.query.grammar.types.Expr;
import com.mysema.query.grammar.types.Operation;
import com.mysema.query.grammar.types.Path;
import com.mysema.query.grammar.types.PathMetadata;
import com.mysema.query.grammar.types.Expr.EBoolean;
import com.mysema.query.grammar.types.Expr.EConstant;
import com.mysema.query.grammar.types.Expr.EConstructor;
import com.mysema.query.grammar.types.PathMetadata.PathType;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Converter;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.query.AbstractProjectingQuery;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.sesame.SesameSession;
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
    
    private Stack<Op<?>> operatorStack = new Stack<Op<?>>();

    private final Map<Path<?>, Var> pathToVar = new HashMap<Path<?>, Var>();
    
    private final Map<Path<?>, Var> pathToMatchedVar = new HashMap<Path<?>,Var>();

    private ProjectionElemList projection = new ProjectionElemList();
    
    private List<OrderElem> orderElements = new ArrayList<OrderElem>(); 

    private TupleQueryResult queryResult;

    private final Map<UID, Var> resToVar = new HashMap<UID, Var>();
    
    private final Map<Object,Var> constToVar = new HashMap<Object,Var>();
    
    private VarNameIterator varNames = new VarNameIterator("v");
    
    private final SesameOps sailOps = SesameOps.DEFAULT;
    
    private final SesameSession sesameSession;
    
    private JoinBuilder statementPatterns = new JoinBuilder();

    private final TypeConverter typeConverter = new SimpleTypeConverter();
    
    private boolean includeInferred = true;
        
    public SesameQuery(SesameSession session, StatementPattern.Scope patternScope) {
        super(session.getDialect(), session);
        this.sesameSession = session; 
        this.patternScope = patternScope;
    }
    
    public SesameQuery(SesameSession session) {
        this(session, StatementPattern.Scope.DEFAULT_CONTEXTS);    
    }  

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
            } catch (QueryEvaluationException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
        
    @SuppressWarnings("unchecked")
    protected <RT> RT convert(Class<RT> rt, Literal literal) {
        return (RT) typeConverter.convertIfNecessary(literal.stringValue(), rt);
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
            ParsedTupleQuery query;
            if (getMetadata().isDistinct()){
                query = new ParsedTupleQuery(new Distinct(tupleExpr));
            }else{
                query = new ParsedTupleQuery(tupleExpr);
            }
            
            logQuery(query);
            
            // TODO : replace the following two lines with proper Sesame integration
            SesameQueryHolder.set(query);
            TupleQuery tupleQuery = sesameSession.getConnection().prepareTupleQuery(SesameQueryHolder.QUERYDSL, "");                       
            tupleQuery.setIncludeInferred(includeInferred);
            
            queryResult =  tupleQuery.evaluate();
            return new Iterator<Value[]>(){
                public boolean hasNext() {
                    try {                        
                        return queryResult.hasNext();
                    } catch (QueryEvaluationException e) {
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
                    } catch (QueryEvaluationException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
                public void remove() {
                    // do nothing
                }                
            };
        } catch (RepositoryException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (MalformedQueryException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (QueryEvaluationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }        
    }

    protected void logQuery(ParsedTupleQuery query) {
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
        ID id = sesameSession.getIdentityService().getID(new LID(lid));
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
            Converter converter = sesameSession.getConverter();
            if (javaValue instanceof Class){
                rdfValue = getTypeForJavaClass((Class<?>)javaValue);
            }else if (converter.supports(javaValue.getClass())){
                String label = converter.toString(javaValue);
                UID datatype = converter.getDatatype(javaValue);
                rdfValue = dialect.getLiteral(label, dialect.getURI(datatype));
            }else{
                rdfValue = sesameSession.getId(javaValue);
            }        
            Var var = new Var(varNames.next(), rdfValue);
            var.setAnonymous(true);
            constToVar.put(javaValue, var);
            return var;
        }        
    }
    
    private ValueExpr transformOperation(Operation<?,?> operation) {
        Op<?> op = operation.getOperator();
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
        if (pathType.equals(PROPERTY)) {
            mappedPath = getMappedPathForPropertyPath(path);   
        }else{
            mappedPath = getMappedPathForPropertyPath(path.getMetadata().getParent());
        }
        Locale locale = null;
        if (!mappedPath.getPredicatePath().isEmpty()){
            if (mappedPath.getMappedProperty().isLocalized()){
                String value = operation.getArg(1).toString();
                if (pathType.equals(PROPERTY)){
                    locale = sesameSession.getCurrentLocale();
                }else if (pathType.equals(MAPVALUE_CONSTANT)){
                    locale = ((EConstant<Locale>)path.getMetadata().getExpression()).getConstant();                        
                }else{
                    throw new IllegalArgumentException("Unsupported path type " + pathType);
                }
                constValue = sesameSession.getDialect().getLiteral(value, locale);
                
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
        
    private ValueExpr transformSizeCompareConstant(Operation<?, ?> operation, Op<?> op) {
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
        // path to last
        Var pathVar = transformPath(((Path<?>)operation.getArg(0)).getMetadata().getParent());                        
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

    private boolean isPathInPath(Operation<?, ?> operation, Op<?> op) {
        return op.equals(Ops.IN) 
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Path;
    }

    private boolean isPathEqPath(Operation<?, ?> operation, Op<?> op) {
        return  Ops.equalsOps.contains(op)
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof Path;
    }
    
    private boolean isPathEqNeConstant(Operation<?, ?> operation, Op<?> op) {
        return (Ops.equalsOps.contains(op) || Ops.notEqualsOps.contains(op)) 
                && operation.getArg(0) instanceof Path 
                && operation.getArg(1) instanceof EConstant;
    }
    
    private boolean isSizeCompareConstant(Operation<?, ?> operation, Op<?> op) {
        return Ops.compareOps.contains(op) 
                && operation.getArg(0) instanceof Path 
                && ((Path<?>)operation.getArg(0)).getMetadata().getPathType() == PathMetadata.SIZE;
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
            
            if (pathType.equals(PROPERTY)) {
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

            } else if (pathType.equals(ARRAY_SIZE) || pathType.equals(SIZE)) {
                throw new UnsupportedOperationException(pathType + " not supported!");
                
            } else if (pathType.equals(ARRAYVALUE) || pathType.equals(LISTVALUE)) {
                // ?!? 
                throw new UnsupportedOperationException(pathType + " not supported!");
                
            } else if (pathType.equals(ARRAYVALUE_CONSTANT) || pathType.equals(LISTVALUE_CONSTANT)) {
                @SuppressWarnings("unchecked")
                int index = getIntValue((EConstant<Integer>)md.getExpression());                
                for (int i = 0; i < index; i++){
                    pathNode = new Var(varNames.next());
                    match(parentNode, RDF.rest, pathNode);
                    parentNode = pathNode;
                }
                pathNode = new Var(varNames.next());
                match(parentNode, RDF.first, pathNode);

            } else if (pathType.equals(MAPVALUE) || pathType.equals(MAPVALUE_CONSTANT)) {     
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
