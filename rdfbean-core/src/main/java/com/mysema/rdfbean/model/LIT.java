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

    private final String value; // not null
    
    private final Locale lang;
    
    private final UID datatype; // not null

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        result = prime * result + datatype.hashCode();
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof LIT)) {
            return false;
        }

        LIT other = (LIT) obj;
        
        if (!value.equals(other.value)) {
            return false;
        }
        
        if (lang == null) {
            if (other.lang != null) {
                return false;
            }
        } else if (!lang.equals(other.lang)) {
            return false;
        }

        if (!datatype.equals(other.datatype)) {
            return false;
        }

        return true;
    }
    
}
