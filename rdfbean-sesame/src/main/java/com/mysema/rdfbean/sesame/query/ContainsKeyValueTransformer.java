/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedProperty;

/**
 * ContainsKeyValueTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ContainsKeyValueTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.CONTAINS_KEY, Ops.CONTAINS_VALUE);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Path<?> path = (Path<?>) operation.getArg(0);
        Var pathVar = context.toVar(path);
        MappedPath mappedPath = context.getMappedPath(path); 
        if (!mappedPath.getMappedProperty().isLocalized()){
            Var valNode, keyNode;
            if (operation.getOperator().equals(Ops.CONTAINS_KEY)){
                keyNode = (Var) context.toValue(operation.getArg(1));
                valNode = null;                        
            }else{                    
                keyNode = null;
                valNode = (Var) context.toValue(operation.getArg(1));
            }                
            return transformMapAccess(pathVar, mappedPath, valNode, keyNode, context);
        }else{  
            // TODO
            return null;
        }
    }
    
    @Nullable
    private ValueExpr transformMapAccess(Var pathVar, MappedPath mappedPath, 
            @Nullable Var valNode, @Nullable Var keyNode, TransformerContext context) {
        MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
        JoinBuilder builder = context.createJoinBuilder();
        if (valNode != null){
            if (mappedProperty.getValuePredicate() != null){
                context.match(builder, pathVar, mappedProperty.getValuePredicate(), valNode);
            }else if (!context.inNegation()){    
                pathVar.setValue(valNode.getValue());
            }
        }
        if (keyNode != null){
            context.match(builder, pathVar, mappedProperty.getKeyPredicate(), keyNode);   
        }                        
        
        if (!builder.isEmpty()){
            return new Exists(builder.getTupleExpr()); 
        }else if (context.inNegation()){
            return new Compare(pathVar, valNode, CompareOp.EQ);
        }else{
            return null;
        }        
    }

}
