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
import com.mysema.rdfbean.annotations.MapElements;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedProperty;

/**
 * @author tiwe
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
            UID c = context.getContext(path);
            return transformMapAccess(pathVar, mappedPath, valNode, keyNode, context, c);

        }else{
            // TODO
            return null;
        }
    }

    @Nullable
    private ValueExpr transformMapAccess(Var pathVar, MappedPath mappedPath,
            @Nullable Var valNode, @Nullable Var keyNode, TransformerContext context, @Nullable UID c) {
        MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
        JoinBuilder builder = context.createJoinBuilder();
        if (valNode != null){
            if (mappedProperty.getValuePredicate() != null){
                MapElements mapKey = mappedProperty.getAnnotation(MapElements.class);
                if (mapKey.value().includeInferred()){
                    c = null;
                }else if (!mapKey.value().context().isEmpty()){
                    c = new UID(mapKey.value().context());
                }
                context.match(builder, pathVar, mappedProperty.getValuePredicate(), valNode, c);
            }else if (!context.inNegation()){
                pathVar.setValue(valNode.getValue());
            }
        }
        if (keyNode != null){
            MapElements mapKey = mappedProperty.getAnnotation(MapElements.class);
            if (mapKey.key().includeInferred()){
                c = null;
            }else if (!mapKey.key().context().isEmpty()){
                c = new UID(mapKey.key().context());
            }
            context.match(builder, pathVar, mappedProperty.getKeyPredicate(), keyNode, c);
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
