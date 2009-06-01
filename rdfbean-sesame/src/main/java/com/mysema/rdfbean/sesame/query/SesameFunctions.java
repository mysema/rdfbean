/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.rdfbean.query.Constants;
import com.mysema.rdfbean.sesame.query.SesameOps.Transformer;

/**
 * SailFunctions provides Op -> Function mappings for Sesame query creation
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameFunctions {
    
    private static final DatatypeFactory datatypeFactory;

    private static final Map<Operator<?>,String> opToFunctionURI = new HashMap<Operator<?>,String>();
    
    static{
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        // STRING FUNCTIONS
        register(
            new StringFunction("trim", Ops.TRIM){
                protected String convert(Value... args){    
                        return args[0].stringValue().trim();
                }  
            },   
            new StringFunction("upper", Ops.UPPER){
                protected String convert(Value... args){
                    return args[0].stringValue().toUpperCase();
                }                        
            },
            new StringFunction("lower", Ops.LOWER){
                protected String convert(Value... args){
                    return args[0].stringValue().toLowerCase();
                }                        
            },
            new StringFunction("concat", Ops.CONCAT){
                protected String convert(Value... args){
                    return args[0].stringValue() + args[1].stringValue();
                }            
            },
            new StringFunction("substring", Ops.SUBSTR1ARG, Ops.SUBSTR2ARGS){
                protected String convert(Value... args){
                    if (args.length == 2){
                        return args[0].stringValue().substring(Integer.valueOf(args[1].stringValue()));    
                    }else{
                        return args[0].stringValue().substring(Integer.valueOf(args[1].stringValue()), Integer.valueOf(args[2].stringValue()));
                    }                
                }
            },
            new StringFunction("space", Ops.StringOps.SPACE){
                protected String convert(Value... args){
                    return StringUtils.leftPad("", Integer.valueOf(args[1].stringValue()));
                }                        
            },
            
            // CHAR FUNCTIONS
            new StringFunction("charAt", Ops.CHAR_AT){
                @Override
                protected String convert(Value... args){
                    return String.valueOf(args[0].stringValue().charAt(Integer.valueOf(args[1].stringValue())));
                }            
            },
            
            // BOOLEAN FUNCTIONS
            new BooleanFunction("contains", Ops.CONTAINS){
                protected boolean convert(Value... args){
                    return args[0].stringValue().contains(args[1].stringValue());
                }                        
            },
            new BooleanFunction("equalsIgnoreCase", Ops.EQ_IGNORECASE){
                protected boolean convert(Value... args){
                    return args[0].stringValue().equalsIgnoreCase(args[1].stringValue());
                }                        
            },
            new BooleanFunction("empty", Ops.COL_ISEMPTY){
                protected boolean convert(Value... args){
                    return args[0].stringValue().length() == 0;
                }                        
            },
                    
            // INTEGER FUNCTIONS
            new IntegerFunction("length", Ops.STRING_LENGTH){
                protected int convert(Value... args){
                    return args[0].stringValue().length();
                }                        
            },
            new IntegerFunction("indexOf", Ops.INDEXOF, Ops.INDEXOF_2ARGS){
                protected int convert(Value... args){
                    if (args.length == 2){
                        return args[0].stringValue().indexOf(args[1].stringValue());    
                    }else{
                        return args[0].stringValue().indexOf(args[1].stringValue(), Integer.valueOf(args[2].stringValue()));
                    }      
                }                        
            },
            new IntegerFunction("lastIndexOf", Ops.LAST_INDEX, Ops.LAST_INDEX_2ARGS){
                protected int convert(Value... args){
                    if (args.length == 2){
                        return args[0].stringValue().lastIndexOf(args[1].stringValue());    
                    }else{
                        return args[0].stringValue().lastIndexOf(args[1].stringValue(), Integer.valueOf(args[2].stringValue()));
                    }      
                }                        
            });
        
        // OTHER NUMERIC
        
        register(
                new BaseFunction("ceil", Ops.MathOps.CEIL){
                    @Override
                    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                        return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
                    }                    
                },
                new BaseFunction("floor", Ops.MathOps.FLOOR){
                    @Override
                    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                        return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
                    }                    
                },
                new BaseFunction("abs", Ops.MathOps.ABS){
                    @Override
                    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                        if (args[0].stringValue().startsWith("-")){
                            Literal l = (Literal)args[0];
                            return valueFactory.createLiteral(l.stringValue().substring(1), l.getDatatype());
                        }else{
                            return args[0];
                        }
                    }
                    
                }
                
        );
        
        // OTHER DATE / TIME
        
        register(
                new IntegerFunction("year", Ops.DateTimeOps.YEAR){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getYear();
                    }                    
                },
                new IntegerFunction("month", Ops.DateTimeOps.MONTH){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getMonth();
                    }                    
                },
                new IntegerFunction("day", Ops.DateTimeOps.DAY){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getDay();
                    }                    
                },
                new IntegerFunction("hour", Ops.DateTimeOps.HOUR){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getHour();
                    }                    
                },
                new IntegerFunction("minute", Ops.DateTimeOps.MINUTE){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getMinute();
                    }                    
                },
                new IntegerFunction("second", Ops.DateTimeOps.SECOND){
                    protected int convert(Value... args) {
                        return datatypeFactory.newXMLGregorianCalendar(args[0].stringValue()).getSecond();
                    }                    
                }    
        );
    }

    public static void addTransformers(Map<Operator<?>, Transformer> byOp) {
        for(final Map.Entry<Operator<?>,String> e : opToFunctionURI.entrySet()){
            byOp.put(e.getKey(), new Transformer(){
                @Override
                public ValueExpr transform(List<ValueExpr> args) {
                    return new FunctionCall(e.getValue(), args);
                }                
            });
        }
        
    }
    
    private static void register(BaseFunction... functions){
        for (BaseFunction function : functions){
            FunctionRegistry.getInstance().add(function);
            for (Operator<?> op : function.getOps()){
                opToFunctionURI.put(op, function.getURI());    
            }    
        }                
    }
    
    /**     
     * Base class for Function implementations
     */
    static abstract class BaseFunction implements Function{
        private final String uri;
        private final Operator<?>[] ops;
        public BaseFunction(String ln, Operator<?>...ops){
            this.uri = Constants.NS + ln;
            this.ops = ops;
        }
        
        @Override
        public final String getURI() {
            return uri;
        }        
        public final Operator<?>[] getOps(){
            return ops;
        }
    }
    
    /**     
     * String typed function
     */
    static abstract class StringFunction extends BaseFunction{
        public StringFunction(String uri, Operator<?>...ops){
            super(uri, ops);
        }
        @Override
        public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
            return valueFactory.createLiteral(convert(args));
        }
        protected abstract String convert(Value... args);
    }
    
    /**     
     * Boolean typed function
     */
    static abstract class BooleanFunction extends BaseFunction{
        public BooleanFunction(String uri, Operator<?>...ops){
            super(uri, ops);
        }
        @Override
        public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
            return valueFactory.createLiteral(convert(args));
        }
        protected abstract boolean convert(Value... args);    
    }
    
    /**     
     * Integer typed function
     */
    static abstract class IntegerFunction extends BaseFunction{
        public IntegerFunction(String uri, Operator<?>...ops){
            super(uri, ops);
        }
        @Override
        public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
            return valueFactory.createLiteral(convert(args));
        }
        protected abstract int convert(Value... args);    
    }
    
}
