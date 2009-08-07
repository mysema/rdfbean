/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query.functions;

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
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.ConverterRegistry;
import com.mysema.rdfbean.query.QDSL;
import com.mysema.rdfbean.sesame.query.Transformer;

/**
 * SesameFunctions provides Op -> Function mappings for Sesame query creation
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameFunctions {
    
    private final Map<Operator<?>, String> opToFunctionURI = new HashMap<Operator<?>, String>();

    public SesameFunctions(final ConverterRegistry converter) {
        
        // STRING FUNCTIONS
        
        register(new StringFunction(QDSL.trim, Ops.TRIM) {
            protected String convert(Value... args) {
                return args[0].stringValue().trim();
            }
        });
        register(new StringFunction(QDSL.upper, Ops.UPPER) {
            protected String convert(Value... args) {
                return args[0].stringValue().toUpperCase();
            }
        });
        register(new StringFunction(QDSL.lower, Ops.LOWER) {
            protected String convert(Value... args) {
                return args[0].stringValue().toLowerCase();
            }
        });
        register(new StringFunction(QDSL.concat, Ops.CONCAT) {
            protected String convert(Value... args) {
                return args[0].stringValue() + args[1].stringValue();
            }
        });
        register(new StringFunction(QDSL.substring, Ops.SUBSTR1ARG,
                Ops.SUBSTR2ARGS) {
            protected String convert(Value... args) {
                if (args.length == 2) {
                    return args[0].stringValue().substring(
                            Integer.valueOf(args[1].stringValue()));
                } else {
                    return args[0].stringValue().substring(
                            Integer.valueOf(args[1].stringValue()),
                            Integer.valueOf(args[2].stringValue()));
                }
            }
        });
        register(new StringFunction(QDSL.space, Ops.StringOps.SPACE) {
            protected String convert(Value... args) {
                return StringUtils.leftPad("", Integer.valueOf(args[1]
                        .stringValue()));
            }
        });
        register(new StringFunction(QDSL.charAt, Ops.CHAR_AT) {
            @Override
            protected String convert(Value... args) {
                return String.valueOf(args[0].stringValue().charAt(
                        Integer.valueOf(args[1].stringValue())));
            }
        });
        register(new BooleanFunction(QDSL.startsWith) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().startsWith(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QDSL.endsWith) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().endsWith(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QDSL.startsWithIc) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().toLowerCase().startsWith(args[1].stringValue().toLowerCase());
            }
        });
        register(new BooleanFunction(QDSL.endsWithIc) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().toLowerCase().endsWith(args[1].stringValue().toLowerCase());
            }
        });
        register(new BooleanFunction(QDSL.stringContains, Ops.STRING_CONTAINS) {
            protected boolean convert(Value... args) {
                return args[0].stringValue().contains(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QDSL.equalsIgnoreCase, Ops.EQ_IGNORECASE) {
            protected boolean convert(Value... args) {
                return args[0].stringValue().equalsIgnoreCase(
                        args[1].stringValue());
            }
        });
        register(new IntegerFunction(QDSL.stringLength, Ops.STRING_LENGTH) {
            protected int convert(Value... args) {
                return args[0].stringValue().length();
            }
        });
        register(new IntegerFunction(QDSL.indexOf, Ops.INDEXOF, Ops.INDEXOF_2ARGS) {
            protected int convert(Value... args) {
                if (args.length == 2) {
                    return args[0].stringValue().indexOf(args[1].stringValue());
                } else {
                    return args[0].stringValue().indexOf(args[1].stringValue(),
                            Integer.valueOf(args[2].stringValue()));
                }
            }
        });

        // NUMERIC

        register(new BaseFunction(QDSL.ceil, Ops.MathOps.CEIL) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.floor, Ops.MathOps.FLOOR) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.sqrt, Ops.MathOps.SQRT) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.sqrt(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QDSL.abs, Ops.MathOps.ABS) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                if (args[0].stringValue().startsWith("-")) {
                    Literal l = (Literal) args[0];
                    return valueFactory.createLiteral(l.stringValue().substring(1), l.getDatatype());
                } else {
                    return args[0];
                }
            }
        });
        
        // CASTS
        
        register(new BaseFunction(XSD.byteType) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Byte.valueOf(args[0].stringValue()));
            }
        });
        register(new BaseFunction(XSD.longType) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Long.valueOf(args[0].stringValue()));
            }
        });
        register(new BaseFunction(XSD.shortType) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Long.valueOf(args[0].stringValue()));
            }
        });

        // DATE / TIME

        register(new IntegerFunction(QDSL.year, Ops.DateTimeOps.YEAR) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getYear();
            }
        });
        register(new IntegerFunction(QDSL.month, Ops.DateTimeOps.MONTH) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getMonthOfYear();
            }
        });
        register(new IntegerFunction(QDSL.dayOfMonth, Ops.DateTimeOps.DAY_OF_MONTH) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getDayOfMonth();
            }
        });
        register(new IntegerFunction(QDSL.hour, Ops.DateTimeOps.HOUR) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getHourOfDay();
            }
        });
        register(new IntegerFunction(QDSL.minute, Ops.DateTimeOps.MINUTE) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getMinuteOfHour();
            }
        });
        register(new IntegerFunction(QDSL.second, Ops.DateTimeOps.SECOND) {
            protected int convert(Value... args) {
                return converter.fromString(args[0].stringValue(), DateTime.class).getSecondOfMinute();
            }
        });
    }

    public void addTransformers(Map<Operator<?>, Transformer> byOp) {
        for (final Map.Entry<Operator<?>, String> e : opToFunctionURI
                .entrySet()) {
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

}
