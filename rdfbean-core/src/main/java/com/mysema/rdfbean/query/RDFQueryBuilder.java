package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Operation;
import com.mysema.query.types.OperationImpl;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.PathType;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.TemplateExpressionImpl;
import com.mysema.query.types.Templates;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.query.types.template.NumberTemplate;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.BooleanQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 *
 */
public class RDFQueryBuilder {
    
    private static final Templates TEMPLATES = new Templates(){
        {
            add(PathType.PROPERTY, "{0}_{1}");
            add(PathType.COLLECTION_ANY, "{0}");
            add(PathType.LISTVALUE_CONSTANT, "{0}_{1}");
            add(PathType.ARRAYVALUE_CONSTANT, "{0}_{1}");
            add(PathType.MAPVALUE_CONSTANT, "{0}_{1}");
        }};    
    
    private final RDFConnection connection;
    
    private final Session session;
    
    private final Configuration configuration;
    
    private final QueryMetadata metadata;
    
    private final List<Expression<?>> projection;
    
    private final VarNameIterator varNames = new VarNameIterator("_var_");
    
    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();
    
    private final Map<Path<?>, UID> pathToContext = new HashMap<Path<?>, UID>();
    
    private Map<Path<?>, Path<?>> pathToMapped = new HashMap<Path<?>, Path<?>>();
    
    public RDFQueryBuilder(RDFConnection connection,
            Session session,
            Configuration configuration, 
            QueryMetadata metadata) {
        this.connection = connection;
        this.session = session;
        this.configuration = configuration;
        this.metadata = metadata;
        this.projection = new ArrayList<Expression<?>>();
    }

    RDFQueryImpl build(boolean forCount){
        RDFQueryImpl query = new RDFQueryImpl(connection);
        Filters filters = new Filters();
        //from 
        for (JoinExpression join : metadata.getJoins()){
            query.where(handleRootPath((Path<?>) join.getTarget()));
        }        
        
        //where
        if (metadata.getWhere() != null){
            query.where(transform(filters, metadata.getWhere()));
        }        
        
        //group by
        for (Expression<?> expr : metadata.getGroupBy()){
            query.groupBy(transform(filters, expr));
        }        
        
        //having
        if (metadata.getHaving() != null){
            query.having(transform(filters, metadata.getHaving()));
        }                
        filters.beginOptional();
                        
        //order by (optional paths)
        for (OrderSpecifier<?> os : metadata.getOrderBy()){
            query.orderBy(transform(filters, os));
        }        
        
        if (forCount){
            // TODO : alternatively COUNT(*), if supported
            projection.add(NumberTemplate.create(Integer.class, "?counter"));
        }else{
            // limit + offset
            query.restrict(metadata.getModifiers());
                    
            //select (optional paths);
            for (Expression<?> expr : metadata.getProjection()){
                projection.add(transform(filters, expr));
            }
        }
        
        filters.endOptional();
        query.where(filters.toArray());
        return query;
    }
    

    private Expression<?> transformQuery(Filters filters, SubQueryExpression<?> expr) {
        QueryMetadata md = expr.getMetadata();        
        Filters f = new Filters();
        
        // from
        for (JoinExpression join : md.getJoins()){
            f.add(handleRootPath((Path<?>) join.getTarget()));
        }            
        // where
        if (md.getWhere() != null){
            f.add(transform(f, md.getWhere()));
        } 
        // select
        // TODO
        
        return f.asBlock();
    }

    @Nullable
    private Predicate transform(Filters filters, Predicate where) {
        return (Predicate) transform(filters, (Expression<?>)where);
    }
    
    @Nullable
    private Expression<?> transform(Filters filters, Expression<?> expr) {
        if (expr instanceof Path<?>){
            return transformPath(filters, (Path<?>)expr);
        }else if (expr instanceof Operation<?>){
            return transformOperation(filters, (Operation<?>)expr);
        }else if (expr instanceof TemplateExpression<?>){
            return transformTemplate(filters, (TemplateExpression<?>)expr);
        }else if (expr instanceof Constant<?>){     
            return transformConstant(filters, (Constant<?>)expr);
        }else if (expr instanceof SubQueryExpression<?>){
            return transformQuery(filters, (SubQueryExpression<?>)expr);
        }else{
            throw new IllegalArgumentException(expr.toString());    
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

    private boolean inOptionalPath(){
        return operatorStack.contains(Ops.IS_NULL) ||
        operatorStack.contains(Ops.MAP_IS_EMPTY) ||
        (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    private Expression<?> transformConstant(Filters filters, Constant<?> constant){
        Object javaValue = constant.getConstant();
        ConverterRegistry converter = configuration.getConverterRegistry();
        if (List.class.isAssignableFrom(constant.getType()) && ((List<?>)constant.getConstant()).isEmpty()){
            return new ConstantImpl<UID>(RDF.nil);
            
        }else if (NODE.class.isAssignableFrom(constant.getType())){
            return constant;
            
        }else if (javaValue instanceof Class<?>){
            UID datatype = converter.getDatatype((Class<?>)javaValue);
            if (datatype != null){
                return new ConstantImpl<UID>(datatype);
            }else{
                return new ConstantImpl<UID>(getTypeForDomainClass((Class<?>)javaValue));
            }
            
        }else if (converter.supports(javaValue.getClass())){
            String label = converter.toString(javaValue);
            UID datatype = converter.getDatatype(javaValue.getClass());
            return new ConstantImpl<LIT>(new LIT(label, datatype));
            
        }else{
            ID id = session.getId(javaValue);
            return new ConstantImpl<ID>(id);
        }
    }
    
    protected UID getTypeForDomainClass(Class<?> clazz){        
        MappedClass mc = configuration.getMappedClass(clazz);
        if (mc.getUID() != null){
            return mc.getUID();
        }else{
            throw new IllegalArgumentException("Got no RDF type for " + clazz.getName());
        }
    }
    
    private Expression<?> transformPath(Filters filters, Path<?> path){
        if (pathToMapped.containsKey(path)){
            return pathToMapped.get(path);
            
        }else if (path.getMetadata().getParent() != null){
            PathMetadata<?> md = path.getMetadata();
            PathType pathType = md.getPathType();
            Path<?> parent = (Path<?>) transformPath(filters, md.getParent());
            Path<?> pathNode = null;
            Path<?> rdfPath = pathToMapped.get(path);
            UID context = getContext(path);
            
            if (pathType.equals(PathType.PROPERTY)){
                MappedPath mappedPath = getMappedPath(path);
                List<MappedPredicate> predPath = mappedPath.getPredicatePath();
                if (predPath.size() > 0){
                    MappedPredicate last = predPath.get(predPath.size()-1);
                    if (last.includeInferred()){
                        pathToContext.put(path, context = null);
                    }else if (last.getContext() != null){
                        pathToContext.put(path, context = last.getContext());
                    }else if (configuration.isMapped(path.getType())){
                        MappedClass mappedClass = configuration.getMappedClass(path.getType());
                        if (mappedClass.getClass() != null){
                            pathToContext.put(path, mappedClass.getContext());
                        }
                    }
                    for (int i = 0; i < predPath.size(); i++){
                        MappedPredicate pred = predPath.get(i);
                        UID c = pred.getContext() != null ? pred.getContext() : context;
                        if (rdfPath != null && (i == predPath.size()-1)){
                            pathNode = rdfPath;
                        }else if (i == predPath.size() -1){
                            pathNode = var(path.accept(ToStringVisitor.DEFAULT, TEMPLATES));
                            varNames.disallow(path.toString());
                        }else{
                            pathNode = var(varNames.next());
                        }
                        if (!pred.inv()){
                            filters.add(Blocks.pattern(parent, pred.getUID(), pathNode, c));
                        }else{
                            filters.add(Blocks.pattern(pathNode, pred.getUID(), parent, c));
                        }
                        parent = pathNode;
                    }
                    
                }else{
                    pathNode = parent;
                }
                
            }else if (pathType.equals(PathType.COLLECTION_ANY)){
                return transformPath(filters, path.getMetadata().getParent());
                
            }else if (pathType.equals(PathType.ARRAYVALUE) 
                    || pathType.equals(PathType.LISTVALUE)){
                throw new UnsupportedOperationException(pathType + " not supported!");
                
            }else if (pathType.equals(PathType.ARRAYVALUE)
                    || pathType.equals(PathType.LISTVALUE_CONSTANT)){
                int index = ((Constant<Integer>)md.getExpression()).getConstant().intValue();
                for (int i = 0; i < index; i++){
                    pathNode = var(varNames.next());
                    filters.add(Blocks.pattern(parent, RDF.rest, pathNode, context));
                    parent = pathNode;
                }
                pathNode = var(varNames.next());
                filters.add(Blocks.pattern(parent, RDF.first, pathNode, context));
                
            }else if (pathType.equals(PathType.MAPVALUE)
                    || pathType.equals(PathType.MAPVALUE_CONSTANT)){
                MappedPath mappedPath = getMappedPath(md.getParent());
                MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                if (!mappedProperty.isLocalized()){
                    Expression<?> expr = transform(filters, path.getMetadata().getExpression());
                    filters.add(Blocks.pattern(parent, mappedProperty.getKeyPredicate(), expr, context));
                    
                    if (mappedProperty.getValuePredicate() != null){
                        pathNode = var(varNames.next());
                        filters.add(Blocks.pattern(parent, mappedProperty.getValuePredicate(), pathNode, context));
                    }else{
                        pathNode = parent;
                    }
                    
                }else{
                    pathNode = parent;
                }
                
            }
            
            pathToMapped.put(path, pathNode);
            return pathNode;
            
        }else{
            throw new IllegalArgumentException("Undeclared path " + path);
        }
        
    }
    
    private Path<NODE> var(String var){
        return new PathImpl<NODE>(NODE.class, var);
    }
    
    private MappedPath getMappedPath(Path<?> path) {
        PathMetadata<?> md = path.getMetadata();
        if (path.getMetadata().getPathType() != PathType.PROPERTY){
            md = md.getParent().getMetadata();    
        }
        MappedClass mc = configuration.getMappedClass(md.getParent().getType());   
        return mc.getMappedPath(md.getExpression().toString());
           
    }
    
    @Nullable
    private UID getContext(Path<?> path){
        if (pathToContext.containsKey(path)){
            return pathToContext.get(path);
        }else if (path.getMetadata().getParent() != null){
            return getContext(path.getMetadata().getParent());
        }else{
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Expression<?> transformOperation(Filters filters, Operation<?> operation){
        boolean filtersInOptional = filters.inOptional();
        boolean outerOptional = inOptionalPath();
        boolean innerOptional = false;
        operatorStack.push(operation.getOperator());
        Map<Path<?>, Path<?>> origPathToMapped = null;
        if (!outerOptional && inOptionalPath()){
            filters.beginOptional();
            innerOptional = true;
            
            origPathToMapped = pathToMapped;
        }
        
        List<Expression<?>> args = new ArrayList<Expression<?>>(operation.getArgs().size());
        
        try{
            if (operation.getOperator() == Ops.EQ_OBJECT || operation.getOperator() == Ops.NE_OBJECT){
                if (operation.getArg(0) instanceof Path && ((Path)operation.getArg(0)).getMetadata().getPathType() != PathType.VARIABLE){
                    if (operation.getArg(1) instanceof Constant){
                        Path<?> path = (Path<?>) operation.getArg(0);
                        Constant<?> constant = (Constant<?>) operation.getArg(1);
                        MappedPath mappedPath = getMappedPath(path);
                        // id property
                        if (path.getMetadata().getPathType() == PathType.PROPERTY 
                            && constant.getType().equals(String.class)
                            && mappedPath.getPredicatePath().isEmpty()){                            
                            operation = new PredicateOperation((Operator)operation.getOperator(), 
                                    path, 
                                    new ConstantImpl(session.getId(new LID(constant.toString()))));
                            
                        // localized property
                        }else if (mappedPath.getMappedProperty().isLocalized()){
                            Locale locale;
                            if (path.getMetadata().getPathType() == PathType.PROPERTY){
                                locale = session.getCurrentLocale();
                            }else{
                                locale = (Locale)((Constant)path.getMetadata().getExpression()).getConstant();
                            } 
                            operation = new PredicateOperation((Operator)operation.getOperator(), 
                                    path, 
                                    new ConstantImpl(new LIT(constant.toString(), locale)));
                        }
                    }    
                }
                
                
            }else if (operation.getOperator() == Ops.AND){
                Predicate lhs = (Predicate) transform(filters, operation.getArg(0));
                Predicate rhs = (Predicate) transform(filters, operation.getArg(1));
                return lhs == null ? rhs : (rhs == null ? rhs : ExpressionUtils.and(lhs, rhs));
                    
            }else if (operation.getOperator() == Ops.IN){
                if (operation.getArg(0) instanceof Path && operation.getArg(1) instanceof Path){
                    operation = (Operation)ExpressionUtils.eq(operation.getArg(0), (Expression)operation.getArg(1));
                }else if (operation.getArg(0) instanceof Path && operation.getArg(1) instanceof Constant){
                    Collection col = (Collection)((Constant)operation.getArg(1)).getConstant();
                    if (!col.isEmpty()){
                        BooleanBuilder builder = new BooleanBuilder();
                        for (Object o : col){
                            builder.or(ExpressionUtils.eq(operation.getArg(0), new ConstantImpl(o)));
                        }
                        operation = (Operation)builder.getValue();    
                    }else{
                        throw new IllegalArgumentException(operation.toString());
                    }
                    
                }else if (operation.getArg(0) instanceof Constant && operation.getArg(1) instanceof Path){
                    operation = (Operation)ExpressionUtils.eq(operation.getArg(0), (Expression)operation.getArg(1));
                }else{
                    throw new IllegalArgumentException(operation.toString());
                }
                
            }else if (operation.getOperator() == Ops.BETWEEN){
                operation = (Operation<?>) ExpressionUtils.and(
                        new PredicateOperation(Ops.GOE, operation.getArg(0), operation.getArg(1)), 
                        new PredicateOperation(Ops.LOE, operation.getArg(0), operation.getArg(2)));
                
            }else if (operation.getOperator() == Ops.ORDINAL){
                Path<?> path = (Path<?>) transform(filters, operation.getArg(0));
                Path<?> ordinalPath = new PathImpl<LIT>(LIT.class, path.toString()+"_ordinal");
                filters.add(Blocks.pattern(path, CORE.enumOrdinal, ordinalPath));
                return ordinalPath;
                
            }else if (operation.getOperator() == Ops.INSTANCE_OF){
                Path<?> path = (Path<?>) transform(filters, operation.getArg(0));
                Constant<?> type = (Constant<?>) transform(filters, operation.getArg(1));
                // TODO : handle negation properly
                filters.add(Blocks.pattern(path, RDF.type, type));
                return null;
                
            }else if (operation.getOperator() == Ops.COL_IS_EMPTY){
                operation = (Operation<?>) ExpressionUtils.eq(operation.getArg(0), new ConstantImpl(RDF.nil));
            
            }else if (operation.getOperator() == Ops.CONTAINS_KEY){
                Path<?> path = (Path<?>) operation.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();
                Expression<?> key = transform(filters, operation.getArg(1));
                // TODO : handle negation properly
                filters.add(Blocks.pattern(transform(filters, path), mappedProperty.getKeyPredicate(), key));
                return null;
                
            }else if (operation.getOperator() == Ops.CONTAINS_VALUE){
                Path<?> path = (Path<?>) operation.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();
                
                // TODO : handle negation properly
                if (mappedProperty.getValuePredicate() != null){
                    Expression<?> value = transform(filters, operation.getArg(1));
                    filters.add(Blocks.pattern(transform(filters, path), mappedProperty.getValuePredicate(), value));
                    return null;    
                    
                }else{
                    operation = (Operation<?>) ExpressionUtils.eq((Path)path, operation.getArg(1));
                }
                
                
            }else if (operation.getOperator() == Ops.MAP_IS_EMPTY){
                // TODO : handle negation properly
                filters.add(new PredicateOperation(Ops.IS_NULL, transform(filters, operation.getArg(0))));
                return null;
            }
                
            for (Expression<?> arg : operation.getArgs()){
                Expression<?> transformed = transform(filters, arg);
                if (transformed != null){
                    args.add(transformed);
                }else{
                    System.err.println(arg + " skipped");
                }
            }
        }finally{
            operatorStack.pop();
            if (!outerOptional && innerOptional){
                if (!filtersInOptional){
                    filters.endOptional();
                }
                pathToMapped = origPathToMapped;
            }
        }
                
        if (operation.getType().equals(Boolean.class)){
            return BooleanOperation.create((Operator)operation.getOperator(), args.toArray(new Expression[args.size()]));
        }else{
            return new OperationImpl(operation.getType(),operation.getOperator(), args);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private Expression<?> transformTemplate(Filters filters, TemplateExpression<?> template){
        List<Expression<?>> args = new ArrayList<Expression<?>>(template.getArgs().size());
        for (Expression<?> arg : args){
            Expression<?> transformed = transform(filters, arg);
            if (transformed != null){
                args.add(transformed);
            }
        }
        if (template.getType().equals(Boolean.class)){
            return new BooleanTemplate(template.getTemplate(), args);
        }else{
            return new TemplateExpressionImpl(template.getType(), template.getTemplate(), args);    
        }        
    }

    @SuppressWarnings("unchecked")
    private OrderSpecifier<?> transform(Filters filters, OrderSpecifier<?> os) {
        Expression<?> expr = transform(filters, os.getTarget());
        return new OrderSpecifier(os.getOrder(), expr);
    }

    private Block handleRootPath(Path<?> path) {
        MappedClass mappedClass = configuration.getMappedClass(path.getType());
        UID rdfType = mappedClass.getUID();
        UID context = mappedClass.getContext();
        pathToMapped.put(path, path);
        if (rdfType != null){
            if (context != null){
                pathToContext.put(path, context);
                return Blocks.pattern(path, RDF.type, rdfType, context);
            }else{
                return Blocks.pattern(path, RDF.type, rdfType);
            }
        } else {
            throw new IllegalArgumentException("No types mapped against " + path.getType().getName());
        }
        
    }

    public BooleanQuery createBooleanQuery() {
        return build(false).createBooleanQuery();
    }

    public TupleQuery createTupleQuery(boolean forCount) {
        return build(forCount).createTupleQuery(projection.toArray(new Expression[0]));
    }
       

}
