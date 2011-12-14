package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import com.mysema.query.JoinExpression;
import com.mysema.query.QueryFlag;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.QueryFlag.Position;
import com.mysema.query.support.SerializerBase;
import com.mysema.query.types.*;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * @author tiwe
 *
 */
public class SPARQLVisitor extends SerializerBase<SPARQLVisitor> implements RDFVisitor<Void,Void>{

    @Nullable
    private PatternBlock lastPattern;

    private final String prefix;

    private final Stack<Operator<?>> operators = new Stack<Operator<?>>();

    private boolean inlineAll = false;
    
    private boolean inlineResources = false;
    
    private boolean likeAsMatches = false;
    
//    private boolean resource = false;

    @Nullable
    private QueryMetadata metadata;
    
    public SPARQLVisitor() {
        this(SPARQLTemplates.DEFAULT, "");
    }

    public SPARQLVisitor(SPARQLTemplates templates, String prefix) {
        super(templates);
        this.prefix = prefix;
    }

    @Override
    protected void appendAsString(Expression<?> expr) {
        Object constant;
        if (expr instanceof Constant<?>){
            constant = ((Constant<?>)expr).getConstant();
        }else if (expr instanceof ParamExpression<?> && metadata != null){
            if (metadata.getParams().containsKey(expr)){
                constant = metadata.getParams().get(expr);
            }else{
                constant = ((ParamExpression<?>)expr).getName();
            }

        }else{
            constant = expr.toString();
        }
        if (constant instanceof NODE){
            append(((NODE)constant).getValue());
        }else{
            append(constant.toString());
        }
    }

    @Nullable
    public Void visit(QueryMetadata md, QueryLanguage<?,?> queryType) {
        metadata = md;
        QueryModifiers mod = md.getModifiers();
        append(prefix);
        Set<QueryFlag> flags = metadata.getFlags();

        // start
        serialize(Position.START, flags);

        // select
        if (queryType == QueryLanguage.TUPLE){
            append("SELECT ");
            if (md.isDistinct()){
                append("DISTINCT ");
            }
            if (!md.getProjection().isEmpty()){
                for (Expression<?> expr : md.getProjection()){
                    if (expr instanceof TemplateExpression<?> || expr instanceof com.mysema.query.types.Operation<?>){
                        append("(").handle(expr).append(")");
                    }else{
                        handle(expr);
                    }
                    append(" ");
                }
            }else{
                append("*");
            }
            append("\n");

        // ask
        }else if (queryType == QueryLanguage.BOOLEAN){
            append("ASK ");

        // construct
        }else if (queryType == QueryLanguage.GRAPH){
            if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof GroupBlock){
                append("CONSTRUCT ").handle("", md.getProjection()).append("\n");
            }else{
                append("CONSTRUCT { ").handle("", md.getProjection()).append("}\n");
            }
        }
        lastPattern = null;

        // from
        for (JoinExpression je : md.getJoins()){
            UID uid = (UID)((Constant<?>)je.getTarget()).getConstant();
            append("FROM <").append(uid.getId()).append(">\n");
        }

        // where
        if (md.getWhere() != null){
            if (queryType != QueryLanguage.BOOLEAN){
                append("WHERE \n  ");
            }
            if (md.getWhere() instanceof GroupBlock){
                handle(md.getWhere());
            }else{
                append("{ ").handle(md.getWhere()).append("}");
            }
            append("\n");
        }

        // order
        if (!md.getOrderBy().isEmpty()){
            append("ORDER BY ");
            boolean first = true;
            for (OrderSpecifier<?> order : md.getOrderBy()){
                if (!first){
                    append(" ");
                }
                if (order.isAscending()){
                    handle(order.getTarget());
                }else{
                    append("DESC(").handle(order.getTarget()).append(")");
                }
                first = false;
            }
            append("\n");
        }

        // group by
        if (!md.getGroupBy().isEmpty()){
            append("GROUP BY ").handle(" ", md.getGroupBy()).append("\n");
        }

        // having
        if (md.getHaving() != null){
            append("HAVING (").handle(md.getHaving()).append(")\n");
        }

        // limit
        if (mod.getLimit() != null){
            append("LIMIT ").append(mod.getLimit().toString()).append("\n");
        }

        // offset
        if (mod.getOffset() != null){
            append("OFFSET ").append(mod.getOffset().toString()).append("\n");
        }

        metadata = null;
        return null;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Void visit(SubQueryExpression<?> expr, Void context) {
        for (Map.Entry<ParamExpression<?>, Object> entry : metadata.getParams().entrySet()){
            expr.getMetadata().setParam((ParamExpression)entry.getKey(), entry.getValue());
        }

        if (!operators.isEmpty() && operators.peek() == Ops.EXISTS){
            handle(expr.getMetadata().getWhere());
        }else{
            visit(expr.getMetadata(), QueryLanguage.TUPLE);
        }

        return null;
    }

    @Nullable
    public Void visit(UnionBlock expr, @Nullable Void context) {
        lastPattern = null;
        boolean first = true;
        for (Block block : expr.getBlocks()){
            if (!first){
                append("UNION ");
            }
            if (block instanceof PatternBlock){
                append("{ ").handle(block).append("} ");
            }else{
                handle(block);
            }
            lastPattern = null;
            first = false;
        }
        return null;
    }

    @Nullable
    public Void visit(GroupBlock expr, @Nullable Void context) {
        lastPattern = null;
        append("{ ");
        visitBlocks(expr.getBlocks());
        visitFilter(expr.getFilters());
        append("} ");
        lastPattern = null;
        return null;
    }

    @Nullable
    public Void visit(GraphBlock expr, @Nullable Void context) {
        lastPattern = null;
        append("GRAPH ").handle(expr.getContext()).append(" { ");
        visitBlocks(expr.getBlocks());
        visitFilter(expr.getFilters());
        append("} ");
        lastPattern = null;
        return null;
    }

    @Nullable
    public Void visit(OptionalBlock expr, @Nullable Void context) {
        lastPattern = null;
        append("OPTIONAL { ");
        visitBlocks(expr.getBlocks());
        visitFilter(expr.getFilters());
        append("} ");
        lastPattern = null;
        return null;
    }

    private void visitBlocks(List<Block> blocks){
        for (Block block : blocks){
            if (lastPattern != null && !(block instanceof PatternBlock)){
                append(".\n  ");
                lastPattern = null;
            }
            handle(block);
        }
    }

    private void visitFilter(@Nullable Predicate filter){
        if (filter != null){
            if (lastPattern != null){
                append(". ");
            }
            lastPattern = null;
            append("FILTER(").handle(filter).append(") ");
        }
        lastPattern = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void visit(Constant<?> expr, Void context) {
        
        // convert literal values to LIT objects
        if (expr.getType().equals(String.class)) {
            expr = new ConstantImpl<LIT>(LIT.class, new LIT(expr.getConstant().toString()));
        }else if (ConverterRegistryImpl.DEFAULT.supports(expr.getType())) {
            UID datatype = ConverterRegistryImpl.DEFAULT.getDatatype(expr.getType());
            String value = ConverterRegistryImpl.DEFAULT.toString(expr.getConstant());
            expr = new ConstantImpl<LIT>(LIT.class, new LIT(value, datatype));
        }        
        
        if (expr.getConstant() instanceof QueryMetadata) {
            QueryMetadata md = (QueryMetadata)expr.getConstant();
            handle(md.getWhere());

        }else if (expr.getConstant() instanceof Block) {
            handle((Expression<?>)expr.getConstant());

        }else if (inlineAll && NODE.class.isInstance(expr.getConstant())){
            NODE node = (NODE)expr.getConstant();
            if (node.isBNode()){
                append("_:" + node.getValue());
            }else if (node.isURI()){
                append("<"+ node.getValue() + ">");
            }else{
                append(node.toString());
            }
            
        }else if (inlineResources && UID.class.isInstance(expr.getConstant())) {
            UID node = (UID)expr.getConstant();
            append("<" + node.getValue() + ">");

        } else if (Collection.class.isAssignableFrom(expr.getType())) {    
            boolean first = true;
            append("(");
            for (Object o : ((Constant<Collection>)expr).getConstant()){
                if (!first){
                    append(", ");
                }
                visit(new ConstantImpl<Object>(o), context);
                first = false;
            }
            append(")");
            
        }else if (!getConstantToLabel().containsKey(expr.getConstant())) {
            String constLabel = "_c" + (getConstantToLabel().size() + 1);
            getConstantToLabel().put(expr.getConstant(), constLabel);
            append("?" + constLabel);
            
        } else {
            append("?" + getConstantToLabel().get(expr.getConstant()));
        }
        return null;
    }

    @Nullable
    public Void visit(PatternBlock expr, @Nullable Void context) {
//        resource = true;
        if (lastPattern == null || !lastPattern.getSubject().equals(expr.getSubject())){
            if (lastPattern != null){
                append(".\n  ");
            }
            handle(expr.getSubject()).append(" ");
            handle(expr.getPredicate()).append(" ");

        }else if (!lastPattern.getPredicate().equals(expr.getPredicate())){
            append("; ");
            handle(expr.getPredicate()).append(" ");

        }else{
            append(", ");
        }

        handle(expr.getObject()).append(" ");
        lastPattern = expr;
//        resource = false;
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void visitOperation(Class<?> type, Operator<?> operator, List<? extends Expression<?>> args) {
        if (operator == Ops.LIKE && likeAsMatches && args.get(1) instanceof Constant){
            operator = Ops.MATCHES;
            String value = ((Constant<LIT>)args.get(1)).getConstant().getValue().replace("%", ".*").replace("_", ".");
            args = Arrays.asList(args.get(0), new ConstantImpl<LIT>(LIT.class, new LIT(value)));
        }
        operators.push(operator);
        try{
            if (operator == Ops.NUMCAST){
                UID datatype = (UID) ((Constant<?>)args.get(1)).getConstant();
                append("xsd:"+datatype.ln()+"(");
                handle(args.get(0));
                append(")");
            }else{
                super.visitOperation(type, operator, args);
            }
        }finally{
            operators.pop();
        }
    }

    @Override
    @Nullable
    public Void visit(ParamExpression<?> param, @Nullable Void context){
        getConstantToLabel().put(param, param.getName());
        append("?"+param.getName());
        return null;
    }

    public void addBindings(SPARQLQuery query, QueryMetadata md) {
        for (Map.Entry<Object,String> entry : getConstantToLabel().entrySet()){
            if (entry.getKey() instanceof ParamExpression<?>){
                if (md.getParams().containsKey(entry.getKey())){
                    query.setBinding(entry.getValue(), (NODE)md.getParams().get(entry.getKey()));
                }
            }else{
                query.setBinding(entry.getValue(), (NODE)entry.getKey());
            }
        }
    }

    public void setInlineResources(boolean b) {
        inlineResources = b;
    }

    public void setLikeAsMatches(boolean likeAsMatches) {
        this.likeAsMatches = likeAsMatches;
    }

    public void setInlineAll(boolean inlineAll) {
        this.inlineAll = inlineAll;
    }
    
}
