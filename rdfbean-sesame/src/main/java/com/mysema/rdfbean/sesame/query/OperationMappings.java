/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.types.operation.Ops.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;

import com.mysema.query.types.operation.Operator;
import com.mysema.rdfbean.object.ConverterRegistry;
import com.mysema.rdfbean.object.ConverterRegistryImpl;
import com.mysema.rdfbean.query.QDSL;
import com.mysema.rdfbean.sesame.query.functions.SesameFunctions;

/**
 * SesameOps provides Operator -> ValueExpr mappings for Sesame query creation
 *
 * @author tiwe
 * @version $Id$
 */
public class OperationMappings {
    
    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();
    
    private final SesameFunctions functions =  new SesameFunctions(converterRegistry);
    
    private final Map<Operator<?>,Transformer> opToTransformer = new HashMap<Operator<?>,Transformer>();
    
    public OperationMappings(){        
        // BOOLEAN
        opToTransformer.put(AND, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args){
                if (args.get(0) == null){
                    return args.get(1);
                }else if (args.get(1) == null){
                    return args.get(0);
                }else{
                    return new And(args.get(0), args.get(1));    
                }                
            }            
        });        
        opToTransformer.put(OR, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args){
                return new Or(args.get(0), args.get(1));
            }            
        });
        opToTransformer.put(NOT, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args){
                return new Not(args.get(0));
            }            
        });        
        
        // COMPARISON        
        Iterator<CompareOp> compareOps = Arrays.asList(
                CompareOp.EQ, CompareOp.EQ, CompareOp.NE, CompareOp.NE, 
                CompareOp.LT, CompareOp.LE, CompareOp.GT, CompareOp.GE).iterator();
        for (Operator<?> op : Arrays.<Operator<?>>asList(
                EQ_OBJECT, 
                EQ_PRIMITIVE, 
                NE_OBJECT, 
                NE_PRIMITIVE, 
                LT, 
                LOE, 
                GT, 
                GOE)){            
            opToTransformer.put(op, new CompareTransformer(compareOps.next()));
        }        
        opToTransformer.put(AFTER, new CompareTransformer(CompareOp.GT));
        opToTransformer.put(BEFORE, new CompareTransformer(CompareOp.LT));
        opToTransformer.put(AOE, new CompareTransformer(CompareOp.GE));
        opToTransformer.put(BOE, new CompareTransformer(CompareOp.LE));
        opToTransformer.put(BETWEEN, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new And(
                    new Compare(args.get(0), args.get(1),CompareOp.GE),    
                    new Compare(args.get(0), args.get(2),CompareOp.LE));
            }            
        });
//        opToTransformer.put(NOTBETWEEN, new Transformer(){
//            @Override
//            public ValueExpr transform(List<ValueExpr> args) {
//                return new Or(
//                    new Compare(args.get(0), args.get(1),CompareOp.LT),    
//                    new Compare(args.get(0), args.get(2),CompareOp.GT));
//            }            
//        });
        opToTransformer.put(STARTS_WITH, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                Var arg2 = ((Var)args.get(1));
                if (arg2.getValue() != null){
                    return new Regex(first, ((Var)args.get(1)).getValue().stringValue()+"*",true);
                }else{
                    return new FunctionCall(QDSL.startsWith.getId(), args);
                }
            }            
        });
        opToTransformer.put(ENDS_WITH, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                Var arg2 = ((Var)args.get(1));
                if (arg2.getValue() != null){
                    return new Regex(first, "*"+((Var)args.get(1)).getValue().stringValue(),true); 
                }else{
                    return new FunctionCall(QDSL.endsWith.getId(), args);
                }                
            }            
        });
        opToTransformer.put(STRING_CONTAINS, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                Var arg2 = ((Var)args.get(1));
                if (arg2.getValue() != null){
                    return new Regex(first, "*"+((Var)args.get(1)).getValue().stringValue()+"*",true);    
                }else{
                    return new FunctionCall(QDSL.stringContains.getId(), args);
                }
                
            }            
        });
        opToTransformer.put(STRING_IS_EMPTY, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, "", false);  // TODO : optimize          
            }            
        });
//        opToTransformer.put(STRING_ISNOTEMPTY, new Transformer(){
//            @Override
//            public ValueExpr transform(List<ValueExpr> args) {
//                ValueExpr first = new Str(args.get(0));
//                return new Not(new Regex(first, "", false)); // TODO : optimize           
//            }            
//        });
        opToTransformer.put(STARTS_WITH_IC, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                Var arg2 = ((Var)args.get(1));
                if (arg2.getValue() != null){
                    return new Regex(first, ((Var)args.get(1)).getValue().stringValue()+"*",false);    
                }else{
                    return new FunctionCall(QDSL.startsWithIc.getId(), args);
                }
                
            }            
        });
        opToTransformer.put(ENDS_WITH_IC, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                Var arg2 = ((Var)args.get(1));
                if (arg2.getValue() != null){
                    return new Regex(first, "*"+((Var)args.get(1)).getValue().stringValue(),false);
                }else{
                    return new FunctionCall(QDSL.endsWithIc.getId(), args);
                }
            }            
        });
        opToTransformer.put(MATCHES, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                ValueExpr second = new Str(args.get(1));
                return new Regex(first, second, null);
            }            
        }); 
        
        // MATH        
        Iterator<MathOp> mathOps = Arrays.asList(
                MathOp.PLUS, 
                MathOp.MINUS, 
                MathOp.MULTIPLY, 
                MathOp.DIVIDE).iterator();
        for (Operator<?> op : Arrays.<Operator<?>>asList(ADD, SUB, MULT, DIV)){
            opToTransformer.put(op, new MathExprTransformer(mathOps.next()));
        }
        
        // VARIOUS
        // TODO : aggreate function or something else ?!?
//        opToTransformer.put(Ops.MathOps.MAX, new Transformer(){
//            @Override
//            public ValueExpr transform(List<ValueExpr> args) {
//                return new Max(args.get(0));
//            }            
//        });
//        // TODO : aggreate function or something else ?!?
//        opToTransformer.put(Ops.MathOps.MIN, new Transformer(){
//            @Override
//            public ValueExpr transform(List<ValueExpr> args) {
//                return new Min(args.get(0));
//            }            
//        });
        
        opToTransformer.put(STRING_CAST, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
               return new Str(args.get(0));
            }            
        });
        opToTransformer.put(IS_NULL, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Not(new Bound((Var) args.get(0)));
            }            
        });
        opToTransformer.put(IS_NOT_NULL, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Bound((Var) args.get(0));
            }            
        });
        opToTransformer.put(NUMCAST, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new FunctionCall( ((Var)args.get(1)).getValue().stringValue(), args.get(0));
            }            
        });
        
        functions.addTransformers(opToTransformer);
        
    }
    
    public Transformer getTransformer(Operator<?> op){
        return opToTransformer.get(op);
    }
}
