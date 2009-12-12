/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.query.QueryFunctions;

/**
 * SesameFunctions provides Op -> Function mappings for Sesame query creation
 * 
 * @author tiwe
 * @version $Id$
 */
public final class SesameFunctions {
    
    private SesameFunctions(){}
    
    private static final Map<Operator<?>, String> opToFunctionURI = new HashMap<Operator<?>, String>();
         
    static{        
        register(Ops.TRIM, new BaseFunction("functions:trim"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.trim(args[0].stringValue());
            }            
        });        
        register(Ops.UPPER, new BaseFunction("functions:upper"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.upper(args[0].stringValue());
            }            
        });
        register(Ops.LOWER, new BaseFunction("functions:lower"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.lower(args[0].stringValue());
            }            
        });        
        register(Ops.CONCAT, new BaseFunction("functions:concat"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.concat(args[0].stringValue(), args[1].stringValue());
            }            
        });
        register(Ops.SUBSTR_1ARG, new BaseFunction("functions:substring"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.substring(args[0].stringValue(), args[1].stringValue());
            }            
        });        
        register(Ops.SUBSTR_2ARGS, new BaseFunction("functions:substring2"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.substring(args[0].stringValue(), args[1].stringValue(), args[2].stringValue());
            }            
        });        
        register(Ops.StringOps.SPACE, new BaseFunction("functions:space") {
            protected String evaluate(Value[] args) {
                return QueryFunctions.space(args[0].stringValue());
            }            
        });        
        register(Ops.CHAR_AT, new BaseFunction("functions:charAt") {
            protected String evaluate(Value[] args) {
                return QueryFunctions.charAt(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.STARTS_WITH, new BaseFunction("functions:startsWith", XMLSchema.BOOLEAN) { 
            protected String evaluate(Value[] args) {
                return QueryFunctions.startsWith(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.ENDS_WITH, new BaseFunction("functions:endsWith", XMLSchema.BOOLEAN) { 
            protected String evaluate(Value[] args) {
                return QueryFunctions.endsWithIc(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.STARTS_WITH_IC, new BaseFunction("functions:startsWithIc", XMLSchema.BOOLEAN) { 
            protected String evaluate(Value[] args) {
                return QueryFunctions.startsWithIc(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.ENDS_WITH_IC, new BaseFunction("functions:endsWithIc", XMLSchema.BOOLEAN) { 
            protected String evaluate(Value[] args) {
                return QueryFunctions.endsWithIc(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.STRING_CONTAINS, new BaseFunction("functions:stringContains", XMLSchema.BOOLEAN) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.stringContains(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.EQ_IGNORE_CASE, new BaseFunction("functions:equalsIgnoreCase", XMLSchema.BOOLEAN) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.equalsIgnoreCase(args[0].stringValue(), args[1].stringValue());
            } 
        });        
        register(Ops.STRING_LENGTH, new BaseFunction("functions:stringLength", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.stringLength(args[0].stringValue());
            } 
        });        
        register(Ops.INDEX_OF, new BaseFunction("functions:indexOf", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.indexOf(args[0].stringValue(), args[1].stringValue());
            } 
        });       
        register(Ops.INDEX_OF_2ARGS, new BaseFunction("functions:indexOf2", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.indexOf(args[0].stringValue(), args[1].stringValue(), args[2].stringValue());
            } 
        });
        register(Ops.MathOps.CEIL, new BaseFunction("functions:ceil", XMLSchema.DOUBLE) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.ceil(args[0].stringValue());
            } 
        });        
        register(Ops.MathOps.FLOOR, new BaseFunction("functions:floor", XMLSchema.DOUBLE) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.floor(args[0].stringValue());
            } 
        });        
        register(Ops.MathOps.SQRT, new BaseFunction("functions:sqrt", XMLSchema.DOUBLE) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.sqrt(args[0].stringValue());
            } 
        });        
        register(Ops.MathOps.ABS, new BaseFunction("functions:abs", XMLSchema.DOUBLE) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.abs(args[0].stringValue());
            } 
        });        
        register(new BaseFunction(XSD.byteType.getId(), XMLSchema.BYTE) {
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });        
        register(new BaseFunction(XSD.longType.getId(), XMLSchema.LONG) {
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });        
        register(new BaseFunction(XSD.shortType.getId(), XMLSchema.SHORT) {
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });
        register(Ops.DateTimeOps.YEAR, new BaseFunction("functions:year", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.year(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.MONTH, new BaseFunction("functions:month", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.month(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.WEEK, new BaseFunction("functions:week", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.week(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.DAY_OF_WEEK, new BaseFunction("functions:dayOfWeek", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfWeek(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.DAY_OF_MONTH, new BaseFunction("functions:dayOfMonth", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfMonth(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.DAY_OF_YEAR, new BaseFunction("functions:dayOfYear", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfYear(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.HOUR, new BaseFunction("functions:hour", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.hour(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.MINUTE, new BaseFunction("functions:minute", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.minute(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.SECOND, new BaseFunction("functions:second", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.second(args[0].stringValue());
            } 
        });        
        register(Ops.DateTimeOps.MILLISECOND, new BaseFunction("functions:millisecond", XMLSchema.INT) {
            protected String evaluate(Value[] args) {
                return QueryFunctions.millisecond(args[0].stringValue());
            } 
        });                
        register(Ops.LIKE, new BaseFunction("functions:like"){
            protected String evaluate(Value[] args) {
                return QueryFunctions.like(args[0].stringValue(), args[1].stringValue());
            }            
        });
    }

    public static void addTransformers(Map<Operator<?>, Transformer> byOp) {
        for (final Map.Entry<Operator<?>, String> e : opToFunctionURI.entrySet()) {
            byOp.put(e.getKey(), new Transformer() {
                public ValueExpr transform(List<ValueExpr> args) {
                    return new FunctionCall(e.getValue(), args);
                }
            });
        }

    }

    private static void register(BaseFunction function) {
        FunctionRegistry.getInstance().add(function);
    }
    
    private static void register(Operator<?> op, BaseFunction function) {
        FunctionRegistry.getInstance().add(function);
        opToFunctionURI.put(op, function.getURI());
    }

    private static abstract class BaseFunction implements Function {

        private final String uri;
        
        private final URI datatype;

        public BaseFunction(String uid) {
            this(uid, XMLSchema.STRING);
        }
        
        public BaseFunction(String uid, URI datatype) {
            this.uri = uid;
            this.datatype = datatype;
        }
        
        @Override
        public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
            return vf.createLiteral(evaluate(args), datatype);
        }

        protected abstract String evaluate(Value[] args);

        @Override
        public final String getURI() {
            return uri;
        }
    }

}
