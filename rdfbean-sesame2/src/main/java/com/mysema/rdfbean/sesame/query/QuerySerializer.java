/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.ObjectUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedTupleQuery;

import com.mysema.rdfbean.Namespaces;

/**
 * QuerySerializer serializes ParsedTupleQuery instances to a syntax combining 
 * SeRQL and SPARQL features for optimal readability 
 *
 * @see http://www.openrdf.org/doc/sesame2/2.2.4/users/ch09.html#d0e1398
 * @author tiwe
 * @version $Id$
 */
public class QuerySerializer extends QueryModelVisitorBase<RuntimeException>{
    
    private static final String ALL = " ALL ";

    private static final String ANY = " ANY ";

    private static final String AS = " AS ";

    private static final String ASC = " ASC";

    private static final String BOUND = "BOUND( ";
    
    private static final String COMMA = ", ";

    private static final String COUNT = "COUNT( ";

    private static final String DATATYPE = "DATATYPE( ";

    private static final String DESC = " DESC";

    private static final String DISTINCT = "DISTINCT( ";

    private static final String DISTINCT2 = "DISTINCT ";

    private static final String EQUALS = " = ";

    private static final String EXISTS = "EXISTS( ";

    private static final String EXTENSIONS = "\nEXTENSIONS ";

    private static final String FROM = "\nFROM ";

    private static final String IN = " IN ";

    private static final String IS_B_NODE = "isBNode( ";

    private static final String IS_LITERAL = "isLiteral( ";

    private static final String IS_RESOURCE = "isResource( ";

    private static final String IS_URI = "isURI( ";
   
    private static final String LABEL = "label( ";

    private static final String LANG = "lang( ";

    private static final String LIKE = " like ";
    
    private static final String LIMIT = "\nLIMIT ";

    private static final String MATCH = " match ";

    private static final String MAX = "MAX( ";

    private static final String MIN = "MIN( ";

    private static final String NOT = "NOT ";

    private static final String OFFSET = "\nOFFSET ";
    
    private static final String OPTIONAL = "OPTIONAL ( ";

    private static final String OR = " OR\n  ";

    private static final String ORDER_BY = "\nORDER BY ";

    private static final String PREFIXES = "\nPREFIXES";

    private static final String REGEX = "regex( ";

    private static final String SELECT = "SELECT ";

    private static final String SEMICOLON = " ; ";

    private static final String STR = "str( ";

    private static final String UNION = " UNION ";

    private static final String WHERE = "\nWHERE ";
    
    private final Map<String,String> knownPrefixes = new HashMap<String,String>(Namespaces.DEFAULT);
    
    private final StringBuilder builder = new StringBuilder();
    
    private boolean fromPrinted;
    
    @Nullable
    private StatementPattern lastPattern;
    
    private final Set<String> namespaces = new HashSet<String>();
    
    private boolean usingNsPrinted;
    
    public QuerySerializer(ParsedGraphQuery query, boolean verbose){
        query.getTupleExpr().visit(this);        
        if (!namespaces.isEmpty() && verbose){           
            printNamespaces();
        }
    }
    
    public QuerySerializer(ParsedTupleQuery query, boolean verbose){
        query.getTupleExpr().visit(this);    
        if (!namespaces.isEmpty() && verbose){           
            printNamespaces();
        }
    }
        
    private QuerySerializer append(String str){
        builder.append(str);
        return this;
    }
     
    @Override
    public void meet(And node){
        visit(node.getLeftArg()).append( " AND\n  " ).visit(node.getRightArg());
    }
    
    @Override
    public void meet(Bound node){
        append( BOUND ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Compare node){
        node.getLeftArg().visit(this);
        append( " " ).append(node.getOperator().getSymbol()).append( " " );
        node.getRightArg().visit(this);
    }
    
    @Override
    public void meet(CompareAll node){
        node.getArg().visit(this);
        append( " " ).append(node.getOperator().getSymbol()).append( ALL );
        node.getSubQuery().visit(this);
    }
    
    @Override
    public void meet(CompareAny node){
        node.getArg().visit(this);
        append( " " ).append(node.getOperator().getSymbol()).append( ANY );
        node.getSubQuery().visit(this);
    }
    
    @Override
    public void meet(Count node){
        append( COUNT ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Datatype node){
        append( DATATYPE ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Difference node){
        node.getLeftArg().visit(this);
        append( "\n\nMINUS\n\n" );
        node.getRightArg().visit(this);
    }
    
    @Override
    public void meet(Distinct node){
        if (node.getArg() instanceof Projection){
            meet((Projection)node.getArg(), true);
        }else{
            append( DISTINCT ).visit(node.getArg()).append( " )" );    
        }        
    }
    
    @Override
    public void meet(Exists node){
        lastPattern = null;
        append( EXISTS );
        visit(node.getSubQuery());
        append( " )" );
    }
    
    @Override
    public void meet(Extension node){
        append(EXTENSIONS);
        boolean first = true;
        for (ExtensionElem elem : node.getElements()){
            if (!first){
                append( COMMA );
            }
            append(elem.getName()).append(EQUALS);
            elem.getExpr().visit(this);
            first = false;
        }
        node.getArg().visit(this);
    }
    
    @Override
    public void meet(Filter node){
        append( FROM );
        fromPrinted = true;
        node.getArg().visit(this);
        append( WHERE );
        node.getCondition().visit(this);
    }
    
    private String getReadableURI(String ns, String ln){
        String prefix = knownPrefixes.get(ns);
        if (prefix == null){
            prefix = "ns" + knownPrefixes.size();
            knownPrefixes.put(ns, prefix);
        }
        return prefix + ":" + ln;
    }
    
    @Override
    public void meet(FunctionCall node){        
        URI uri = new URIImpl(node.getURI());
        namespaces.add(uri.getNamespace());
        append(getReadableURI(uri.getNamespace(), uri.getLocalName()));
        append( "( " );
        boolean first = true;
        for (ValueExpr v : node.getArgs()){
            if (!first){
                append( COMMA );
            }
            first = false;
            v.visit(this);
        }
        append( " )" );
    }
    
    @Override
    public void meet(In node){
        node.getArg().visit(this);
        append( IN );
        node.getSubQuery().visit(this);
    }
    
    @Override
    public void meet(Intersection node){        
        node.getLeftArg().visit(this);
        append( "\n\nINTERSECT\n\n" );
        node.getRightArg().visit(this);        
    }
    
    @Override
    public void meet(IsBNode node){
        append( IS_B_NODE ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsLiteral node){
        append( IS_LITERAL ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsResource node){
        append( IS_RESOURCE ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsURI node){
        append( IS_URI ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Join node){
        if (!fromPrinted){
            append( FROM );
            fromPrinted = true;
        }
        node.getLeftArg().visit(this);
        if (node.getRightArg() != null){
            node.getRightArg().visit(this);    
        }        
    }
    
    @Override
    public void meet(Label node){
        append( LABEL ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Lang node){
        append( LANG ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(LangMatches node){
        visit(node.getLeftArg()).append(MATCH).visit(node.getRightArg());
    }
    
    @Override
    public void meet(LeftJoin node){
        if (!fromPrinted){
            append( FROM );
            fromPrinted = true;
        }
        node.getLeftArg().visit(this);        
        append( ".\n ");
        append(OPTIONAL );
        lastPattern = null;
        node.getRightArg().visit(this);
        append( " )" );
    }
    
    @Override
    public void meet(Max node){
        append( MAX ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Min node){
        append( MIN ).visit(node.getArg()).append( " )" );
    }
        
    @Override
    public void meet(Not node){
        append( NOT ).visit(node.getArg());
    }
    
    @Override
    public void meet(Or node){
        node.getLeftArg().visit(this);
        append( OR );
        node.getRightArg().visit(this);
    }
    
    @Override
    public void meet(Order node){
        visit(node.getArg());
        append(ORDER_BY);
        boolean first = true;
        for (OrderElem elem : node.getElements()){
            if (!first){
                append(COMMA);
            }
            visit(elem.getExpr());
            append(elem.isAscending() ? ASC : DESC);
            first = false;
        }
    }
    
    @Override
    public void meet(Projection node){
        meet(node, false);
    }
    

    private void meet(Projection node, boolean distinct){
        append( SELECT );
        if (distinct){
            append(DISTINCT2);
        }
        boolean first = true;
        for (ProjectionElem p : node.getProjectionElemList().getElements()){
            if (!first){
                append( COMMA );
            }            
            if (!p.getSourceName().equals(p.getTargetName())){
                append(p.getSourceName()).append( AS );
            }
            append(p.getTargetName());
            
            first = false;
        }        
        node.getArg().visit(this);
    }
    
    @Override
    public void meet(Like node){
        node.getArg().visit(this);
        append(LIKE);
        append(node.getPattern());
    }
    
    @Override
    public void meet(Regex node){        
        append( REGEX );
        node.getArg().visit(this);
        append( COMMA );
        node.getPatternArg().visit(this);
        if (node.getFlagsArg() != null){
            append( COMMA );
            node.getFlagsArg().visit(this);
        }
        append( " )" );
    }
    
    @Override
    public void meet(Slice node){
        node.getArg().visit(this);
        if (node.getLimit() > -1){
            append(LIMIT + node.getLimit());    
        }
        if (node.getOffset() > 0){
            append(OFFSET + node.getOffset());    
        }        
    }
    
    @Override
    public void meet(StatementPattern node){
        if (!fromPrinted){
            append( FROM );
            fromPrinted = true;
        }
        if (lastPattern != null){
            if (lastPattern.getSubjectVar().equals(node.getSubjectVar())){
                if (lastPattern.getPredicateVar().equals(node.getPredicateVar())){
                    append(COMMA).visit(node.getObjectVar());
                    if (!ObjectUtils.equals(lastPattern.getContextVar(), node.getContextVar())){
                        append( " " ).visit(node.getContextVar());
                    }
                    return;
                }else{
                    append(SEMICOLON).visit(node.getPredicateVar()).append(" ").visit(node.getObjectVar());
                    if (!ObjectUtils.equals(lastPattern.getContextVar(), node.getContextVar())){
                        append( " " ).visit(node.getContextVar());
                    }
                    return;
                }
            }else{
                append(" .\n ");
            }            
        }        
        visit(node.getSubjectVar());
        append( " " ).visit(node.getPredicateVar());
        append( " " ).visit(node.getObjectVar());
        if (node.getContextVar() != null){
            append( " " ).visit(node.getContextVar());
        }
        lastPattern = node;
    }
    
    @Override
    public void meet(Str node){
        append( STR ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Union node){
        visit(node.getLeftArg());
        append(UNION);
        visit(node.getRightArg());
    }
    
    private void meet(Value value){
        if (value instanceof URI){    
            URI uri = (URI) value;
            namespaces.add(uri.getNamespace());
            append(getReadableURI(uri.getNamespace(), uri.getLocalName()));
        
        }else if (value instanceof Literal){
            Literal lit = (Literal) value;
            if (lit.getDatatype() != null){
                append("\"").append(lit.getLabel()).append("\"^^");
                namespaces.add(lit.getDatatype().getNamespace());
                append(getReadableURI(lit.getDatatype().getNamespace(), lit.getDatatype().getLocalName()));
            }else{
                append(lit.toString());
            }
            
        }else{
            append(value.toString());
        }
    }
      
    @Override
    public void meet(ValueConstant node){
        meet(node.getValue());
    }
    
    @Override
    public void meet(Var node){
        Value value = node.getValue();
        if (value == null){
            append("{").append(node.getName()).append("}");            
        }else{    
            meet(value);
        }
    }
    
    private void printNamespaces() {
        for (String ns : namespaces){
            String prefix = knownPrefixes.get(ns);
            if (prefix != null){
                if (!usingNsPrinted){
                    append(PREFIXES);
                    usingNsPrinted = true;
                }
                append("\n  ").append(prefix).append(": <").append(ns).append(">");
            }
        }
    }
    
    public String toString(){
        return builder.toString();
    }

    private QuerySerializer visit(QueryModelNode node){
        node.visit(this);
        return this;
    }

}
