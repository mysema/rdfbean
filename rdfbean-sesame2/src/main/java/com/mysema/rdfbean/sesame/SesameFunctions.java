/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.query.QueryFunctions;

/**
 * @author tiwe
 */
public final class SesameFunctions {

    private static boolean initialized;

    public static void init(){
        if (initialized){
            return;
        }

        register(new BaseFunction("functions:trim"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.trim(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:upper"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.upper(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:lower"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.lower(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:concat"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.concat(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:substring"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.substring(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:substring2"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.substring(args[0].stringValue(), args[1].stringValue(), args[2].stringValue());
            }
        });

        register(new BaseFunction("functions:space") {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.space(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:charAt") {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.charAt(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:startsWith", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.startsWith(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:endsWith", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.endsWithIc(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:startsWithIc", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.startsWithIc(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:endsWithIc", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.endsWithIc(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:stringContains", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.stringContains(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:stringContainsIc", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.stringContainsIc(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:equalsIgnoreCase", XMLSchema.BOOLEAN) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.equalsIgnoreCase(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:stringLength", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.stringLength(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:indexOf", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.indexOf(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:indexOf2", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.indexOf(args[0].stringValue(), args[1].stringValue(), args[2].stringValue());
            }
        });

        register(new BaseFunction("functions:ceil", XMLSchema.DOUBLE) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.ceil(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:floor", XMLSchema.DOUBLE) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.floor(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:sqrt", XMLSchema.DOUBLE) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.sqrt(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:abs", XMLSchema.DOUBLE) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.abs(args[0].stringValue());
            }
        });

        register(new BaseFunction(XSD.byteType.getId(), XMLSchema.BYTE) {
            @Override
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });

        register(new BaseFunction(XSD.longType.getId(), XMLSchema.LONG) {
            @Override
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });

        register(new BaseFunction(XSD.shortType.getId(), XMLSchema.SHORT) {
            @Override
            protected String evaluate(Value[] args) {
                return args[0].stringValue();
            }
        });

        register(new BaseFunction("functions:year", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.year(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:yearMonth", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.yearMonth(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:month", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.month(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:week", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.week(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:dayOfWeek", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfWeek(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:dayOfMonth", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfMonth(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:dayOfYear", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.dayOfYear(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:hour", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.hour(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:minute", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.minute(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:second", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.second(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:millisecond", XMLSchema.INT) {
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.millisecond(args[0].stringValue());
            }
        });

        register(new BaseFunction("functions:like"){
            @Override
            protected String evaluate(Value[] args) {
                return QueryFunctions.like(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new BaseFunction("functions:modulo"){
            @Override
            public String evaluate(Value[] args) {
                return QueryFunctions.modulo(args[0].stringValue(), args[1].stringValue());
            }
        });

        register(new Function(){
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                for (Value arg : args){
                    if (arg != null){
                        return arg;
                    }
                }
                return null;
            }
            @Override
            public String getURI() {
                return "functions:coalesce";
            }
        });

        initialized = true;

    }

    private static void register(Function function) {
        FunctionRegistry.getInstance().add(function);
    }

    private abstract static class BaseFunction implements Function {

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

    private SesameFunctions(){}

}
