/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BeanMap;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathType;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;

/**
 * EqualsTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class EqualsTransformer implements OperationTransformer{

    private final Map<Operator<?>,CompareOp> ops = new HashMap<Operator<?>,CompareOp>();
    
    private final OperationTransformer colSize = new ColSizeTransformer();
    
    public EqualsTransformer() {
        ops.put(Ops.EQ_OBJECT, CompareOp.EQ); 
        ops.put(Ops.EQ_PRIMITIVE, CompareOp.EQ);
        ops.put(Ops.NE_OBJECT,  CompareOp.NE);
        ops.put(Ops.NE_PRIMITIVE, CompareOp.NE); 
    }

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return ops.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Expr<?> arg1 = operation.getArg(0);
        Expr<?> arg2 = operation.getArg(1);
        
        if (arg1 instanceof Path){            
           
            if (arg2 instanceof Path 
                    && Ops.equalsOps.contains(operation.getOperator()) 
                    && !context.inNegation()
                    && !context.inOptionalPath()){
                return pathEqPath((Path<?>)arg1, (Path<?>)arg2, context);                
           
            }else if (arg2 instanceof Constant){
                return pathEqNeConstant(operation.getOperator(), (Path<?>) arg1, arg2, context);
            
            }else if (arg2 instanceof SubQueryExpression){
                ValueExpr lhs = context.toValue(arg1);
                TupleExpr rhs = context.toTuples((SubQueryExpression) arg2);
                return new CompareAll(lhs, rhs, ops.get(operation.getOperator()));            
            }     
            
        }else if (arg1 instanceof Operation
            && ((Operation)arg1).getOperator() == Ops.COL_SIZE
            && arg2 instanceof Constant){
            return colSize.transform(operation, context);
        }
                        
        ValueExpr lhs = context.toValue(arg1);
        ValueExpr rhs = context.toValue(arg2);
        return new Compare(lhs, rhs, ops.get(operation.getOperator()));        
    }    
    
    @Nullable
    private ValueExpr pathEqPath(Path<?> path, Path<?> otherPath, TransformerContext context) {
        if (context.isKnown(path)){
            context.register(otherPath, context.toVar(path));
            context.toVar(otherPath);          
        }else{
            context.register(path, context.toVar(otherPath));
            context.toVar(path);
        }  
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private ValueExpr pathEqNeConstant(Operator<?> op, Path<?> path, Expr<?> constant, TransformerContext context){
        Var pathVar = context.toVar(path);
        Value constValue;
        
        MappedPath mappedPath;
        PathType pathType = path.getMetadata().getPathType();
        if (pathType.equals(PathType.VARIABLE)){
            mappedPath = null;
        }else if (pathType.equals(PathType.PROPERTY)) {
            mappedPath = context.getMappedPath(path);   
        }else{
            mappedPath = context.getMappedPath(path.getMetadata().getParent());
        }
        Locale locale = null;
        if (mappedPath != null && !mappedPath.getPredicatePath().isEmpty()){
            if (mappedPath.getMappedProperty().isLocalized()){
                String value = constant.toString();
                if (pathType.equals(PathType.PROPERTY)){
                    locale = context.getLocale();
                }else if (pathType.equals(PathType.MAPVALUE_CONSTANT)){
                    locale = ((Constant<Locale>)path.getMetadata().getExpression()).getConstant();                        
                }else{
                    throw new IllegalArgumentException("Unsupported path type " + pathType);
                }
                constValue = context.getValueFactory().createLiteral(value, LocaleUtil.toLang(locale));
                
            }else if (Collection.class.isAssignableFrom(constant.getType())){
                throw new UnsupportedOperationException("Unsupported operation : path eq Collection");
            }else{
                constValue = ((Var) context.toValue(constant)).getValue();   
               
            }                                
        }else{
            ID id;
            if (ID.class.isAssignableFrom(constant.getType())){
                id = (ID) ((Constant)constant).getConstant();
            }else if (constant.getType().equals(String.class)){
                id = context.getResourceForLID(((Constant<String>)constant).getConstant());
            }else{
                MappedClass mappedClass = context.getMappedClass(constant.getType());
                BeanMap beanMap = new BeanMap(((Constant)constant).getConstant());
                if (ID.class.isAssignableFrom(mappedClass.getIdProperty().getType())){
                    id = (ID) mappedClass.getIdProperty().getValue(beanMap);
                }else{
                    id = context.getResourceForLID((String)mappedClass.getIdProperty().getValue(beanMap));
                }
            }
            constValue = context.toValue(id);
        }
        
        if (Ops.equalsOps.contains(op)){
            if (!context.inOptionalPath()){
                pathVar.setValue(constValue);
                return null;    
            }else{
                return new Compare(pathVar, context.toVar(constValue), Compare.CompareOp.EQ);
            }
                            
        }else{
            Var constVar = context.toVar(constValue);
            Compare compare = new Compare(pathVar, constVar, Compare.CompareOp.NE);
            if (locale != null){
                Var langVar = context.toVar(context.getValueFactory().createLiteral(LocaleUtil.toLang(locale)));
                return new And(
                    compare, 
                    new Compare(new Lang(pathVar), langVar, Compare.CompareOp.EQ));
            }else{
                return compare;
            }
        }             
    }
    
}
