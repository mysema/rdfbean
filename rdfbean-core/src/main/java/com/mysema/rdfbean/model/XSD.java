/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

/**
 * Namespace file for XML Schema namespace
 * 
 * @author sasa
 * 
 */
public final class XSD {
    public static final String NS = "http://www.w3.org/2001/XMLSchema#";

    public static final UID anyURI = new UID(NS, "anyURI");

    public static final UID booleanType = new UID(NS, "boolean");

    public static final UID byteType = new UID(NS, "byte");

    public static final UID date = new UID(NS, "date");

    public static final UID dateTime = new UID(NS, "dateTime");

    public static final UID decimalType = new UID(NS, "decimal");

    public static final UID doubleType = new UID(NS, "double");

    public static final UID duration = new UID(NS, "duration");

    public static final UID floatType = new UID(NS, "float");

    public static final UID gDay = new UID(NS, "gDay");

    public static final UID gMonth = new UID(NS, "gMonth");

    public static final UID gMonthDay = new UID(NS, "gMonthDay");

    public static final UID gYear = new UID(NS, "gYear");

    public static final UID gYearMonth = new UID(NS, "gYearMonth");

    public static final UID hexBinary = new UID(NS, "hexBinary");

    public static final UID integerType = new UID(NS, "integer");

    public static final UID intType = new UID(NS, "int");

    public static final UID longType = new UID(NS, "long");

    public static final UID shortType = new UID(NS, "short");

    public static final UID stringType = new UID(NS, "string");

    public static final UID time = new UID(NS, "time");

    private XSD() {
    }
}
