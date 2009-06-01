/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Locale;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.query.annotations.Entity;

/**
 * @author sasa
 *
 */
@Entity
public final class LIT extends NODE {
    
    private static final long serialVersionUID = 4279040868676951911L;

    private final String value;
    
    private final Locale lang;
    
    private final UID datatype;

    public LIT(String value, UID datatype) {
        this.value = Assert.notNull(value);
        this.datatype = Assert.notNull(datatype);
        this.lang = null;
    }

    public LIT(String value, String lang) {
        this(value, LocaleUtil.parseLocale(lang));
    }

    public LIT(String value, Locale lang) {
        this.value = Assert.notNull(value);
        this.lang = Assert.notNull(lang);
        this.datatype = RDF.text;
    }

    public LIT(String value) {
        this(value, XSD.stringType);
    }

    public String getValue() {
        return value;
    }

    public Locale getLang() {
        return lang;
    }

    public UID getDatatype() {
        return datatype;
    }
    
    public int hashCode() {
        return hashCode(value, datatype, lang);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof LIT) {
            LIT other = (LIT) obj;
            return nullSafeEquals(this.value, other.value)
                && nullSafeEquals(this.datatype, other.datatype)
                && nullSafeEquals(this.lang, other.lang);
        } else {
            return false;
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LITERAL;
    }
    
    public boolean isText() {
        return lang != null;
    }
    
    public boolean isString() {
        return datatype.equals(XSD.stringType);
    }
    
    public String toString() {
        if (isText()) {
            return "\"" + value + "\"@" + LocaleUtil.toLang(lang);
        } else if (isString()) {
            return "\"" + value + "\"";
        } else {
            return "\"" + value + "\"^^" + datatype;
        }
    }

    @Override
    public boolean isBNode() {
        return false;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public boolean isResource() {
        return false;
    }

    @Override
    public boolean isURI() {
        return false;
    }
    
}
