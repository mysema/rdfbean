/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.ConverterRegistry;
import com.mysema.rdfbean.query.QDSL;

/**
 * SesameFunctions provides Op -> Function mappings for Sesame query creation
 * 
 * @author tiwe
 * @version $Id$
 */
public class Functions {
    
    private final Map<Operator<?>, String> opToFunctionURI = new HashMap<Operator<?>, String>();
    
    public Functions(final ConverterRegistry converter) {
        
        // STRING FUNCTIONS
        
        register(new BaseFunction(QDSL.trim, Ops.TRIM) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().trim());
            }            
        });

        register(new BaseFunction(QDSL.upper, Ops.UPPER) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().toUpperCase());
            }
        });
        register(new BaseFunction(QDSL.lower, Ops.LOWER) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().toLowerCase());
            }
        });
        register(new BaseFunction(QDSL.concat, Ops.CONCAT) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue() + args[1].stringValue());
            }
        });
        register(new BaseFunction(QDSL.substring, Ops.SUBSTR_1ARG, Ops.SUBSTR_2ARGS) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                if (args.length == 2) {
                    return vf.createLiteral(args[0].stringValue().substring(
                            Integer.valueOf(args[1].stringValue())));
                } else {
                    return vf.createLiteral(args[0].stringValue().substring(
                            Integer.valueOf(args[1].stringValue()),
                            Integer.valueOf(args[2].stringValue())));
                }
            }
        });
        register(new BaseFunction(QDSL.space, Ops.StringOps.SPACE) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(StringUtils.leftPad("", Integer.valueOf(args[1].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.charAt, Ops.CHAR_AT) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(String.valueOf(args[0].stringValue().charAt(Integer.valueOf(args[1].stringValue()))));
            }
        });
        register(new BaseFunction(QDSL.startsWith) { 
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().startsWith(args[1].stringValue()));
            }
        });
        register(new BaseFunction(QDSL.endsWith) { 
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().endsWith(args[1].stringValue()));
            }
        });
        register(new BaseFunction(QDSL.startsWithIc) { 
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().toLowerCase().startsWith(args[1].stringValue().toLowerCase()));
            }
        });
        register(new BaseFunction(QDSL.endsWithIc) { 
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().toLowerCase().endsWith(args[1].stringValue().toLowerCase()));
            }
        });
        register(new BaseFunction(QDSL.stringContains, Ops.STRING_CONTAINS) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().contains(args[1].stringValue()));
            }
        });
        register(new BaseFunction(QDSL.equalsIgnoreCase, Ops.EQ_IGNORE_CASE) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().equalsIgnoreCase(args[1].stringValue()));
            }
        });
        register(new BaseFunction(QDSL.stringLength, Ops.STRING_LENGTH) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(args[0].stringValue().length());
            }
        });
        register(new BaseFunction(QDSL.indexOf, Ops.INDEX_OF, Ops.INDEX_OF_2ARGS) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                if (args.length == 2) {
                    return vf.createLiteral(args[0].stringValue().indexOf(args[1].stringValue()));
                } else {
                    return vf.createLiteral(args[0].stringValue().indexOf(args[1].stringValue(),Integer.valueOf(args[2].stringValue())));
                }
            }
        });

        // NUMERIC

        register(new BaseFunction(QDSL.ceil, Ops.MathOps.CEIL) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.floor, Ops.MathOps.FLOOR) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.sqrt, Ops.MathOps.SQRT) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Math.sqrt(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.abs, Ops.MathOps.ABS) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                if (args[0].stringValue().startsWith("-")) {
                    Literal l = (Literal) args[0];
                    return vf.createLiteral(l.stringValue().substring(1), l.getDatatype());
                } else {
                    return args[0];
                }
            }
        });
        
        // CASTS
        
        register(new BaseFunction(XSD.byteType) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Byte.valueOf(args[0].stringValue()));
            }
        });
        register(new BaseFunction(XSD.longType) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Long.valueOf(args[0].stringValue()));
            }
        });
        register(new BaseFunction(XSD.shortType) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(Long.valueOf(args[0].stringValue()));
            }
        });

        // DATE / TIME

        register(new BaseFunction(QDSL.year, Ops.DateTimeOps.YEAR) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getYear());
            }
        });
        register(new BaseFunction(QDSL.month, Ops.DateTimeOps.MONTH) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getMonthOfYear());
            }
        });
        register(new BaseFunction(QDSL.week, Ops.DateTimeOps.WEEK) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getWeekOfWeekyear());
            }
        });
        register(new BaseFunction(QDSL.dayOfWeek, Ops.DateTimeOps.DAY_OF_WEEK) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                int dow = converter.fromString(args[0].stringValue(), DateTime.class).getDayOfWeek();
                return vf.createLiteral(dow == 7 ? 1 : dow + 1);
            }
        });
        register(new BaseFunction(QDSL.dayOfMonth, Ops.DateTimeOps.DAY_OF_MONTH) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getDayOfMonth());
            }
        });
        register(new BaseFunction(QDSL.dayOfYear, Ops.DateTimeOps.DAY_OF_YEAR) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getDayOfYear());
            }
        });
        register(new BaseFunction(QDSL.hour, Ops.DateTimeOps.HOUR) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getHourOfDay());
            }
        });
        register(new BaseFunction(QDSL.minute, Ops.DateTimeOps.MINUTE) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getMinuteOfHour());
            }
        });
        register(new BaseFunction(QDSL.second, Ops.DateTimeOps.SECOND) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getSecondOfMinute());
            }
        });
        register(new BaseFunction(QDSL.millisecond, Ops.DateTimeOps.MILLISECOND) {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                return vf.createLiteral(converter.fromString(args[0].stringValue(), DateTime.class).getMillisOfSecond());
            }
        });
        
        register(new BaseFunction(QDSL.like, Ops.LIKE){
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                String str = args[0].stringValue();
                String match = args[0].stringValue().replace("%", ".*").replaceAll("_", ".");
                return vf.createLiteral(str.matches(match));
            }
        });
    }

    public void addTransformers(Map<Operator<?>, Transformer> byOp) {
        for (final Map.Entry<Operator<?>, String> e : opToFunctionURI.entrySet()) {
            byOp.put(e.getKey(), new Transformer() {
                @Override
                public ValueExpr transform(List<ValueExpr> args) {
                    return new FunctionCall(e.getValue(), args);
                }
            });
        }

    }

    private void register(BaseFunction function) {
        FunctionRegistry.getInstance().add(function);
        for (Operator<?> op : function.getOps()) {
            opToFunctionURI.put(op, function.getURI());
        }
    }

    abstract class BaseFunction implements Function {
        private final Operator<?>[] ops;

        private final String uri;

        public BaseFunction(UID uid, Operator<?>... ops) {
            this.uri = uid.getId();
            this.ops = ops;
        }

        public final Operator<?>[] getOps() {
            return ops;
        }

        @Override
        public final String getURI() {
            return uri;
        }
    }

}
