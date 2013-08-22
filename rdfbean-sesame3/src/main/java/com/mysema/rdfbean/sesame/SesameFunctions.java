/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.model.Literal;
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

    public static void init() {
        if (initialized) {
            return;
        }

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue().trim());
            }

            @Override
            public String getURI() {
                return "functions:trim";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue().toUpperCase());
            }

            @Override
            public String getURI() {
                return "functions:upper";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue().toLowerCase());
            }

            @Override
            public String getURI() {
                return "functions:lower";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first + second);
            }

            @Override
            public String getURI() {
                return "functions:concat";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                int second = Integer.valueOf(args[1].stringValue());
                return valueFactory.createLiteral(first.substring(second));
            }

            @Override
            public String getURI() {
                return "functions:substring";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                int second = Integer.valueOf(args[1].stringValue());
                int third = Integer.valueOf(args[2].stringValue());
                return valueFactory.createLiteral(first.substring(second, third));
            }

            @Override
            public String getURI() {
                return "functions:substring2";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                int first = Integer.valueOf(args[0].stringValue());
                return valueFactory.createLiteral(QueryFunctions.space(first));
            }

            @Override
            public String getURI() {
                return "functions:space";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                int second = Integer.valueOf(args[1].stringValue());
                return valueFactory.createLiteral(String.valueOf(first.charAt(second)));
            }

            @Override
            public String getURI() {
                return "functions:charAt";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first.startsWith(second));
            }

            @Override
            public String getURI() {
                return "functions:startsWith";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first.endsWith(second));
            }

            @Override
            public String getURI() {
                return "functions:endsWith";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue().toLowerCase();
                String second = args[1].stringValue().toLowerCase();
                return valueFactory.createLiteral(first.startsWith(second));
            }

            @Override
            public String getURI() {
                return "functions:startsWithIc";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue().toLowerCase();
                String second = args[1].stringValue().toLowerCase();
                return valueFactory.createLiteral(first.endsWith(second));
            }

            @Override
            public String getURI() {
                return "functions:endsWithIc";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first.contains(second));
            }

            @Override
            public String getURI() {
                return "functions:stringContains";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue().toLowerCase();
                String second = args[1].stringValue().toLowerCase();
                return valueFactory.createLiteral(first.contains(second));
            }

            @Override
            public String getURI() {
                return "functions:stringContainsIc";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first.equalsIgnoreCase(second));
            }

            @Override
            public String getURI() {
                return "functions:equalsIgnoreCase";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue().length());
            }

            @Override
            public String getURI() {
                return "functions:stringLength";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(first.indexOf(second));
            }

            @Override
            public String getURI() {
                return "functions:indexOf";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(second.indexOf(first) + 1);
            }

            @Override
            public String getURI() {
                return "functions:locate";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                int third = Integer.valueOf(args[2].stringValue());
                return valueFactory.createLiteral(first.indexOf(second, third));
            }

            @Override
            public String getURI() {
                return "functions:indexOf2";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                int third = Integer.valueOf(args[2].stringValue()) - 1;
                return valueFactory.createLiteral(second.indexOf(first, third) + 1);
            }

            @Override
            public String getURI() {
                return "functions:locate2";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.ceil(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:ceil";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.floor(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:floor";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.sqrt(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:sqrt";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String normalized = args[0].stringValue();
                URI datatype = ((Literal) args[0]).getDatatype();
                return valueFactory.createLiteral(normalized.startsWith("-") ? normalized.substring(1) : normalized, datatype);
            }

            @Override
            public String getURI() {
                return "functions:abs";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue(), XMLSchema.BYTE);
            }

            @Override
            public String getURI() {
                return XSD.byteType.getId();
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue(), XMLSchema.LONG);
            }

            @Override
            public String getURI() {
                return XSD.longType.getId();
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(args[0].stringValue(), XMLSchema.SHORT);
            }

            @Override
            public String getURI() {
                return XSD.shortType.getId();
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.year(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:year";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.yearMonth(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:yearMonth";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.month(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:month";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.week(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:week";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.dayOfWeek(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:dayOfWeek";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.dayOfMonth(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:dayOfMonth";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.dayOfYear(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:dayOfYear";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.hour(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:hour";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.minute(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:minute";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.second(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:second";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                return valueFactory.createLiteral(QueryFunctions.millisecond(args[0].stringValue()));
            }

            @Override
            public String getURI() {
                return "functions:millisecond";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(QueryFunctions.like(first, second));
            }

            @Override
            public String getURI() {
                return "functions:like";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                String first = args[0].stringValue();
                String second = args[1].stringValue();
                return valueFactory.createLiteral(Integer.valueOf(first) % Integer.valueOf(second));
            }

            @Override
            public String getURI() {
                return "functions:modulo";
            }
        });

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                for (Value arg : args) {
                    if (arg != null) {
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

        register(new Function() {
            @Override
            public Value evaluate(ValueFactory vf, Value... args) throws ValueExprEvaluationException {
                if (args[0].equals(args[1])) {
                    return null;
                } else {
                    return args[0];
                }
            }

            @Override
            public String getURI() {
                return "functions:nullif";
            }
        });

        initialized = true;

    }

    private static void register(Function function) {
        FunctionRegistry.getInstance().add(function);
    }

    private SesameFunctions() {
    }

}
