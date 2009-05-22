/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;

import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.operation.Ops.Op;
import static com.mysema.query.types.operation.Ops.*;

/**
 * SailOps provides Op -> ValueExpr mappings for Sesame query creation
 *
 * @author tiwe
 * @version $Id$
 */
class SesameOps {
    
    public static final SesameOps DEFAULT = new SesameOps();
    
    private final Map<Op<?>,Transformer> byOp = new HashMap<Op<?>,Transformer>();
    
    private SesameOps(){
        
        // BOOLEAN
        byOp.put(AND, new Transformer(){
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
        byOp.put(OR, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args){
                return new Or(args.get(0), args.get(1));
            }            
        });
        byOp.put(NOT, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args){
                return new Not(args.get(0));
            }            
        });        
        byOp.put(XOR, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new And(
                    new Or(args.get(0),args.get(1)), 
                    new Not(new And(args.get(0),args.get(1))));
            }            
        });
        byOp.put(XNOR, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Or(
                    new And(args.get(0),args.get(1)), 
                    new And(new Not(args.get(0)),new Not(args.get(1))));
            }            
        });
        
        // COMPARISON        
        Iterator<CompareOp> compareOps = Arrays.asList(
                CompareOp.EQ, CompareOp.EQ, CompareOp.NE, CompareOp.NE, 
                CompareOp.LT, CompareOp.LE, CompareOp.GT, CompareOp.GE).iterator();
        for (Op<?> op : Arrays.<Op<?>>asList(EQ_OBJECT, EQ_PRIMITIVE, NE_OBJECT, NE_PRIMITIVE, LT, LOE, GT, GOE)){            
            byOp.put(op, new CompareTransformer(compareOps.next()));
        }        
        byOp.put(AFTER, new CompareTransformer(CompareOp.GT));
        byOp.put(BEFORE, new CompareTransformer(CompareOp.LT));
        byOp.put(AOE, new CompareTransformer(CompareOp.GE));
        byOp.put(BOE, new CompareTransformer(CompareOp.LE));
        byOp.put(BETWEEN, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new And(
                    new Compare(args.get(0), args.get(1),CompareOp.GE),    
                    new Compare(args.get(0), args.get(2),CompareOp.LE));
            }            
        });
        byOp.put(NOTBETWEEN, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Or(
                    new Compare(args.get(0), args.get(1),CompareOp.LT),    
                    new Compare(args.get(0), args.get(2),CompareOp.GT));
            }            
        });
        byOp.put(LIKE, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, 
                        ((Var)args.get(1)).getValue().stringValue(),false);
            }            
        });
        byOp.put(STARTSWITH, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, 
                        ((Var)args.get(1)).getValue().stringValue()+"*",true);
            }            
        });
        byOp.put(ENDSWITH, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first,
                        "*"+((Var)args.get(1)).getValue().stringValue(),true);
            }            
        });
        byOp.put(CONTAINS, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first,
                        "*"+((Var)args.get(1)).getValue().stringValue()+"*",true);
            }            
        });
        byOp.put(STARTSWITH_IC, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, 
                        ((Var)args.get(1)).getValue().stringValue()+"*",false);
            }            
        });
        byOp.put(ENDSWITH_IC, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, 
                        "*"+((Var)args.get(1)).getValue().stringValue(),false);
            }            
        });
        byOp.put(MATCHES, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                ValueExpr first = new Str(args.get(0));
                return new Regex(first, args.get(1), null);
            }            
        }); 
        
        // MATH        
        Iterator<MathOp> mathOps = Arrays.asList(MathOp.PLUS, MathOp.MINUS, MathOp.MULTIPLY, MathOp.DIVIDE).iterator();
        for (Op<?> op : Arrays.<Op<?>>asList(ADD, SUB, MULT, DIV)){
            byOp.put(op, new MathExprTransformer(mathOps.next()));
        }
        
        // VARIOUS
        // TODO : aggreate function or something else ?!?
        byOp.put(Ops.OpMath.MAX, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Max(args.get(0));
            }            
        });
        // TODO : aggreate function or something else ?!?
        byOp.put(Ops.OpMath.MIN, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Min(args.get(0));
            }            
        });
        
        byOp.put(STRING_CAST, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
               return new Str(args.get(0));
            }            
        });
        byOp.put(ISNULL, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Not(new Bound((Var) args.get(0)));
            }            
        });
        byOp.put(ISNOTNULL, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new Bound((Var) args.get(0));
            }            
        });
        byOp.put(NUMCAST, new Transformer(){
            @Override
            public ValueExpr transform(List<ValueExpr> args) {
                return new FunctionCall( ((Var)args.get(1)).getValue().stringValue(), args.get(0));
            }            
        });
//        byOp.put(ISTYPEOF, new Transformer(){
//            @Override
//            public ValueExpr transform(List<ValueExpr> args) {
//                return new Exists(new StatementPattern((Var)args.get(0), (Var)args.get(1), (Var)args.get(2)));
//            }            
//        });
        
        SesameFunctions.addTransformers(byOp);
        
    }
    
    public Transformer getTransformer(Op<?> op){
        return byOp.get(op);
    }
    
    private static class CompareTransformer implements Transformer{
        private final CompareOp op;        
        
        CompareTransformer(CompareOp op){
            this.op = op;
        }       
        
        @Override
        public ValueExpr transform(List<ValueExpr> args){
            return new Compare(args.get(0), args.get(1), op);
        }        
    }
    
    private static class MathExprTransformer implements Transformer{
        private final MathExpr.MathOp op;        
        
        MathExprTransformer(MathExpr.MathOp op){
            this.op = op;
        }       
        
        @Override
        public ValueExpr transform(List<ValueExpr> args){
            return new MathExpr(args.get(0), args.get(1), op);
        }        
    }

    interface Transformer{

        public ValueExpr transform(List<ValueExpr> args);
        
    }
}
