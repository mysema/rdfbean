/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query.serializer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.TupleQueryModel;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.query.QDSL;

/**
 * QuerySerializer seriales ParsedTupleQuery instances to a syntax combining 
 * SeRQL and SPARQL features for optimal readability 
 *
 * @see http://www.openrdf.org/doc/sesame2/2.2.4/users/ch09.html#d0e1398
 * @author tiwe
 * @version $Id$
 */
public class QuerySerializer extends QueryModelVisitorBase<RuntimeException>{
    
    private static final Set<String> knownNamespaces = new HashSet<String>(
            Arrays.asList(RDF.NS, RDFS.NS, XSD.NS, OWL.NS));
    
    static{
        Namespaces.register("querydsl", QDSL.NS);
    }
    
    private final StringBuilder builder = new StringBuilder();
    
    private boolean fromPrinted;
    
    private StatementPattern lastPattern;
    
    private final Set<String> namespaces = new HashSet<String>();
    
    private boolean usingNsPrinted;
    
    public QuerySerializer(TupleQueryModel query, boolean verbose){
        query.getTupleExpr().visit(this);
        
        if (!namespaces.isEmpty() && verbose){           
            for (String ns : namespaces){
                String prefix = Namespaces.getPrefix(ns);
                if (prefix != null && !knownNamespaces.contains(ns)){
                    if (!usingNsPrinted){
                        append("\nPREFIXES");
                        usingNsPrinted = true;
                    }
                    append("\n  ").append(prefix).append(": <").append(ns).append(">");
                }
            }
        }
    }
    
    private QuerySerializer append(String str){
        builder.append(str);
        return this;
    }
    
    @Override
    public void meet(And node) throws RuntimeException{
        visit(node.getArg(0)).append( " AND\n  " ).visit(node.getArg(1));
    }
    
    @Override
    public void meet(Bound node) throws RuntimeException{
        append( "BOUND( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Compare node) throws RuntimeException{
        node.getLeftArg().visit(this);
        append( " " ).append(node.getOperator().getSymbol()).append( " " );
        node.getRightArg().visit(this);
    }
    
    @Override
    public void meet(Count node) throws RuntimeException{
        append( "COUNT( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Datatype node) throws RuntimeException{
        append( "DATATYPE( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Difference node) throws RuntimeException{
        node.getLeftArg().visit(this);
        append( "\n\nMINUS\n\n" );
        node.getRightArg().visit(this);
    }
    
    @Override
    public void meet(Distinct node) throws RuntimeException{
        if (node.getArg() instanceof Projection){
            meet((Projection)node.getArg(), true);
        }else{
            append( "DISTINCT( " ).visit(node.getArg()).append( " )" );    
        }        
    }
    
    @Override
    public void meet(Exists node) throws RuntimeException{
        lastPattern = null;
        append( "EXISTS( " );
        visit(node.getSubQuery());
        append( " )" );
    }
    
    @Override
    public void meet(Extension node) throws RuntimeException{
        append("\nEXTENSIONS ");
        boolean first = true;
        for (ExtensionElem elem : node.getElements()){
            if (!first){
                append( "," );
            }
            append(elem.getName()).append(" = ");
            elem.getExpr().visit(this);
            first = false;
        }
        node.getArg().visit(this);
    }
    
    @Override
    public void meet(Filter node) throws RuntimeException{
        append( "\nFROM " );
        fromPrinted = true;
        node.getArg().visit(this);
        append( "\nWHERE " );
        node.getCondition().visit(this);
    }
    
    @Override
    public void meet(FunctionCall node) throws RuntimeException{
//        append( "<" ).append(node.getURI()).append( ">" );        
        URI uri = new URIImpl(node.getURI());
        namespaces.add(uri.getNamespace());
        append(Namespaces.getReadableURI(uri.getNamespace(), uri.getLocalName()));
        append( "( " );
        boolean first = true;
        for (ValueExpr v : node.getArgs()){
            if (!first){
                append( "," );
            }
            first = false;
            v.visit(this);
        }
        append( " )" );
    }
    
    @Override
    public void meet(In node) throws RuntimeException{
        node.getArg().visit(this);
        append( " IN " );
        node.getSubQuery().visit(this);
    }
    
    @Override
    public void meet(Intersection node) throws RuntimeException{        
        node.getLeftArg().visit(this);
        append( "\n\nINTERSECT\n\n" );
        node.getRightArg().visit(this);        
    }
    
    @Override
    public void meet(IsBNode node) throws RuntimeException{
        append( "isBNode( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsLiteral node) throws RuntimeException{
        append( "isLiteral( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsResource node) throws RuntimeException{
        append( "isResource( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(IsURI node) throws RuntimeException{
        append( "isURI( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Join node) throws RuntimeException{
        if (!fromPrinted){
            append( "\nFROM " );
            fromPrinted = true;
        }
        node.getArg(0).visit(this);
        if (node.getArg(1) != null){
            node.getArg(1).visit(this);    
        }        
    }
    
    @Override
    public void meet(Label node) throws RuntimeException{
        append( "label( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Lang node) throws RuntimeException{
        append( "lang( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(LangMatches node) throws RuntimeException{
        visit(node.getLeftArg()).append(" match ").visit(node.getRightArg());
    }
    
//    @Override
//    public void meet(Like node) throws RuntimeException{
//        node.getArg().visit(this);
//        append( " LIKE '" ).append(node.getOpPattern()).append( "' " );
//    }
    
    @Override
    public void meet(LeftJoin node) throws RuntimeException{
        if (!fromPrinted){
            append( "\nFROM " );
            fromPrinted = true;
        }
        node.getLeftArg().visit(this);        
        append( ".\n  OPTIONAL ( " );
        lastPattern = null;
        node.getRightArg().visit(this);
        append( " )" );
    }
    
    
    @Override
    public void meet(Max node) throws RuntimeException{
        append( "MAX( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Min node) throws RuntimeException{
        append( "MIN( " ).visit(node.getArg()).append( " )" );
    }
    
    @Override
    public void meet(Not node) throws RuntimeException{
        append( "NOT " ).visit(node.getArg());
    }
    
    @Override
    public void meet(Or node) throws RuntimeException{
        node.getArg(0).visit(this);
        append( " OR\n  " );
        node.getArg(1).visit(this);
    }
    

    @Override
    public void meet(Order node) throws RuntimeException{
        visit(node.getArg());
        append("\nORDER BY ");
        boolean first = true;
        for (OrderElem elem : node.getElements()){
            if (!first){
                append(", ");
            }
            visit(elem.getExpr());
            append(elem.isAscending() ? " ASC" : " DESC");
            first = false;
        }
    }
    
    @Override
    public void meet(Projection node) throws RuntimeException{
        meet(node, false);
    }
    
    private void meet(Projection node, boolean distinct){
        append( "SELECT " );
        if (distinct) append("DISTINCT ");
        boolean first = true;
        for (ProjectionElem p : node.getProjectionElemList().getElements()){
            if (!first){
                append( ", " );
            }            
            if (!p.getSourceName().equals(p.getTargetName())){
                append(p.getSourceName()).append( " AS " );
            }
            append(p.getTargetName());
            
            first = false;
        }        
        node.getArg().visit(this);
    }
    
    @Override
    public void meet(Regex node) throws RuntimeException{        
        append( "regex( " );
        node.getArg().visit(this);
        append( ", " );
        node.getPatternArg().visit(this);
        if (node.getFlagsArg() != null){
            append( ", " );
            node.getFlagsArg().visit(this);
        }
        append( " )" );
    }
    
    @Override
    public void meet(StatementPattern node) throws RuntimeException{
        if (!fromPrinted){
            append( "\nFROM " );
            fromPrinted = true;
        }
        if (lastPattern != null){
            if (lastPattern.getSubjectVar().equals(node.getSubjectVar())){
                if (lastPattern.getPredicateVar().equals(node.getPredicateVar())){
                    append(" , ").visit(node.getObjectVar());
                    return;
                }else{
                    append(" ; ").visit(node.getPredicateVar()).append(" ").visit(node.getObjectVar());
                    return;
                }
            }else{
                append(" .\n ");
            }            
        }        
        visit(node.getSubjectVar()).append( " " ).visit(node.getPredicateVar()).append( " " ).visit(node.getObjectVar());
        lastPattern = node;
    }
    
    @Override
    public void meet(Str node) throws RuntimeException{
        append( "str( " ).visit(node.getArg()).append( " )" );
    }
        
    private void meet(Value value){
        if (value instanceof URI){    
            URI uri = (URI) value;
            namespaces.add(uri.getNamespace());
            append(Namespaces.getReadableURI(uri.getNamespace(), uri.getLocalName()));
        
        }else if (value instanceof Literal){
            Literal lit = (Literal) value;
            if (lit.getDatatype() != null){
                append("\"").append(lit.getLabel()).append("\"^^");
                namespaces.add(lit.getDatatype().getNamespace());
                append(Namespaces.getReadableURI(lit.getDatatype().getNamespace(), lit.getDatatype().getLocalName()));
            }else{
                append(lit.toString());
            }
            
        }else{
            append(value.toString());
        }
    }
    
    @Override
    public void meet(ValueConstant node) throws RuntimeException{
        meet(node.getValue());
    }
    
    @Override
    public void meet(Var node) throws RuntimeException{
        Value value = node.getValue();
        if (value == null){
            append("{").append(node.getName()).append("}");            
        }else{    
            meet(value);
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
