/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query.functions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.mysema.rdfbean.query.QD;
import com.mysema.rdfbean.sesame.query.Transformer;

/**
 * SailFunctions provides Op -> Function mappings for Sesame query creation
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameFunctions {
    
    private final Map<Operator<?>, String> opToFunctionURI = new HashMap<Operator<?>, String>();

    public SesameFunctions(final ConverterRegistry converter) {
        
        // STRING FUNCTIONS
        register(new StringFunction(QD.trim, Ops.TRIM) {
            protected String convert(Value... args) {
                return args[0].stringValue().trim();
            }
        });
        register(new StringFunction(QD.upper, Ops.UPPER) {
            protected String convert(Value... args) {
                return args[0].stringValue().toUpperCase();
            }
        });
        register(new StringFunction(QD.lower, Ops.LOWER) {
            protected String convert(Value... args) {
                return args[0].stringValue().toLowerCase();
            }
        });
        register(new StringFunction(QD.concat, Ops.CONCAT) {
            protected String convert(Value... args) {
                return args[0].stringValue() + args[1].stringValue();
            }
        });
        register(new StringFunction(QD.substring, Ops.SUBSTR1ARG,
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
        register(new StringFunction(QD.space, Ops.StringOps.SPACE) {
            protected String convert(Value... args) {
                return StringUtils.leftPad("", Integer.valueOf(args[1]
                        .stringValue()));
            }
        });
        register(new StringFunction(QD.charAt, Ops.CHAR_AT) {
            @Override
            protected String convert(Value... args) {
                return String.valueOf(args[0].stringValue().charAt(
                        Integer.valueOf(args[1].stringValue())));
            }
        });
        register(new BooleanFunction(QD.startsWith) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().startsWith(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QD.endsWith) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().endsWith(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QD.startsWithIc) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().toLowerCase().startsWith(args[1].stringValue().toLowerCase());
            }
        });
        register(new BooleanFunction(QD.endsWithIc) { 
            protected boolean convert(Value... args) {
                return args[0].stringValue().toLowerCase().endsWith(args[1].stringValue().toLowerCase());
            }
        });
        register(new BooleanFunction(QD.stringContains, Ops.STRING_CONTAINS) {
            protected boolean convert(Value... args) {
                return args[0].stringValue().contains(args[1].stringValue());
            }
        });
        register(new BooleanFunction(QD.equalsIgnoreCase, Ops.EQ_IGNORECASE) {
            protected boolean convert(Value... args) {
                return args[0].stringValue().equalsIgnoreCase(
                        args[1].stringValue());
            }
        });
        register(new IntegerFunction(QD.stringLength, Ops.STRING_LENGTH) {
            protected int convert(Value... args) {
                return args[0].stringValue().length();
            }
        });
        register(new IntegerFunction(QD.indexOf, Ops.INDEXOF, Ops.INDEXOF_2ARGS) {
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

        register(new BaseFunction(QD.ceil, Ops.MathOps.CEIL) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QD.floor, Ops.MathOps.FLOOR) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.ceil(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QD.sqrt, Ops.MathOps.SQRT) {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args)
                    throws ValueExprEvaluationException {
                return valueFactory.createLiteral(Math.sqrt(Double.valueOf(args[0].stringValue())));
            }
        });
        register(new BaseFunction(QD.abs, Ops.MathOps.ABS) {
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

        register(new IntegerFunction(QD.year, Ops.DateTimeOps.YEAR) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:date
                return converter.fromString(args[0].stringValue(), Date.class).getYear();
            }
        });
        register(new IntegerFunction(QD.month, Ops.DateTimeOps.MONTH) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:date
                return converter.fromString(args[0].stringValue(), Date.class).getMonth();
            }
        });
        register(new IntegerFunction(QD.dayOfMonth, Ops.DateTimeOps.DAY_OF_MONTH) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:date
                return converter.fromString(args[0].stringValue(), Date.class).getDay();
            }
        });
        register(new IntegerFunction(QD.hour, Ops.DateTimeOps.HOUR) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:time
                return converter.fromString(args[0].stringValue(), Date.class).getHours();
            }
        });
        register(new IntegerFunction(QD.minute, Ops.DateTimeOps.MINUTE) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:time
                return converter.fromString(args[0].stringValue(), Date.class).getMinutes();
            }
        });
        register(new IntegerFunction(QD.second, Ops.DateTimeOps.SECOND) {
            protected int convert(Value... args) {
                // TODO : make sure this works also for xsd:time
                return converter.fromString(args[0].stringValue(), Date.class).getSeconds();
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
