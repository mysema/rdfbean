package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
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
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.TemplateExpressionImpl;
import com.mysema.query.types.Templates;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.BooleanQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQuery;
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

    private RDFQuery build(boolean forCount){
        RDFQuery query = new RDFQueryImpl(connection);
        List<Predicate> filters = new ArrayList<Predicate>();
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
        query.where(filters.toArray(new Predicate[filters.size()]));        
        filters = new ArrayList<Predicate>();
        
        //order by (optional paths)
        for (OrderSpecifier<?> os : metadata.getOrderBy()){
            query.orderBy(transform(filters, os));
        }        
        
        //select (optional paths);
        for (Expression<?> expr : metadata.getProjection()){
            projection.add(transform(filters, expr));
        }        
        query.where(filters.toArray(new Predicate[filters.size()]));
        return query;
    }

    private Predicate transform(List<Predicate> filters, Predicate where) {
        if (where instanceof Operation<?>){
            return (Predicate)transformOperation(filters, (Operation<?>)where);
        }else{
            return where;
        }
    }
    
    private Expression<?> transform(List<Predicate> filters, Expression<?> expr) {
        if (expr instanceof Path<?>){
            return transformPath(filters, (Path<?>)expr);
        }else if (expr instanceof Operation<?>){
            return transformOperation(filters, (Operation<?>)expr);
        }else if (expr instanceof TemplateExpression<?>){
            return transformTemplate(filters, (TemplateExpression<?>)expr);
        }else if (expr instanceof Constant<?>){     
            return transformConstant(filters, (Constant<?>)expr);
        }else{
            throw new IllegalArgumentException(expr.toString());    
        }        
    }
    
    private boolean inOptionalPath(){
        return operatorStack.contains(Ops.IS_NULL) ||
        (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    private Expression<?> transformConstant(List<Predicate> filters, Constant<?> constant){
        Object javaValue = constant.getConstant();
        ConverterRegistry converter = configuration.getConverterRegistry();
        if (javaValue instanceof Class<?>){
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
    
    private Expression<?> transformPath(List<Predicate> filters, Path<?> path){
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
                MappedPath mappedPath = getMappedPathForPropertyPath(path);
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
                MappedPath mappedPath = getMappedPathForPropertyPath(md.getParent());
                MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                if (mappedProperty.isLocalized()){
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
        }else{
            throw new IllegalArgumentException("Undeclared path " + path);
        }
        
        return null;
    }
    
    private Path<NODE> var(String var){
        return new PathImpl<NODE>(NODE.class, var);
    }
    
    private MappedPath getMappedPathForPropertyPath(Path<?> path) {
        PathMetadata<?> md = path.getMetadata();
        MappedClass mc = configuration.getMappedClass(md.getParent().getType());   
        return mc.getMappedPath(md.getExpression().toString());   
    }
    
    private UID getContext(Path<?> path){
        if (pathToContext.containsKey(path)){
            return pathToContext.get(path);
        }else if (path.getMetadata().getParent() != null){
            return getContext(path.getMetadata().getParent());
        }else{
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Expression<?> transformOperation(List<Predicate> filters, Operation<?> operation){
        boolean outerOptional = inOptionalPath();
        boolean innerOptional = false;
        operatorStack.push(operation.getOperator());
        Map<Path<?>, Path<?>> origPathToMapped = null;
        if (!outerOptional && inOptionalPath()){
            // set optional
            innerOptional = true;
            
            origPathToMapped = pathToMapped;
        }
        List<Expression<?>> args = new ArrayList<Expression<?>>(operation.getArgs().size());
        for (Expression<?> arg : args){
            Expression<?> transformed = transform(filters, arg);
            if (transformed != null){
                args.add(transformed);
            }
        }
        operatorStack.pop();
        if (!outerOptional && innerOptional){
            // set mandatory
            
            pathToMapped = origPathToMapped;
        }
        
        if (operation.getType().equals(Boolean.class)){
            return BooleanOperation.create((Operator)operation.getOperator(), args.toArray(new Expression[0]));
        }else{
            return new OperationImpl(operation.getType(), operation.getOperator(), args);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private Expression<?> transformTemplate(List<Predicate> filters, TemplateExpression<?> template){
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
    private OrderSpecifier<?> transform(List<Predicate> filters, OrderSpecifier<?> os) {
        Expression<?> expr = transform(filters, os.getTarget());
        return new OrderSpecifier(os.getOrder(), expr);
    }

    private Predicate handleRootPath(Path<?> path) {
        MappedClass mappedClass = configuration.getMappedClass(path.getType());
        UID rdfType = mappedClass.getUID();
        UID context = mappedClass.getContext();
        pathToMapped.put(path, path);
        if (context != null){
            pathToContext.put(path, context);
            return Blocks.pattern(path, RDF.type, rdfType, context);
        }else{
            return Blocks.pattern(path, RDF.type, rdfType);
        }
    }

    public BooleanQuery createBooleanQuery() {
        return build(false).createBooleanQuery();
    }

    public TupleQuery createTupleQuery(boolean forCount) {
        return build(forCount).createTupleQuery(projection.toArray(new Expression[0]));
    }
       

}
