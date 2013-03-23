package com.mysema.rdfbean.virtuoso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import virtuoso.sql.ExtendedString;
import virtuoso.sql.RdfBox;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 * 
 */
public class Converter {

    private final ConverterRegistry converters;

    private final Map<String, UID> datatypes = new HashMap<String, UID>(1024);

    private final Map<String, Locale> locales = new HashMap<String, Locale>(128);

    public Converter(ConverterRegistry converters) {
        this.converters = converters;
    }

    @Nullable
    public NODE toNODE(@Nullable Object val) {
        if (val == null) {
            return null;
        } else if (val instanceof ExtendedString) {
            return toNODE((ExtendedString) val);
        } else if (val instanceof RdfBox) {
            return toNODE((RdfBox) val);
        } else if (val instanceof String) {
            return new LIT(val.toString());
        } else {
            UID datatype = converters.getDatatype(val.getClass());
            if (datatype != null) {
                return new LIT(converters.toString(val), datatype);
            } else {
                throw new IllegalArgumentException("Unkown type " + val.getClass().getName());
            }
        }
    }

    private NODE toNODE(ExtendedString ves) {
        final String value = ves.toString();
        if (ves.getIriType() == ExtendedString.IRI && (ves.getStrType() & 0x01) == 0x01) {
            if (value.startsWith("_:")) {
                return new BID(value.substring(2));
            }
            if (value.indexOf(':') < 0) {
                return new UID(":" + value);
            } else {
                return new UID(value);
            }
        } else if (ves.getIriType() == ExtendedString.BNODE) {
            return new BID(value.substring(9)); // "nodeID://"
        } else {
            return new LIT(value);
        }
    }

    private LIT toNODE(RdfBox rb) {
        if (rb.getLang() != null) {
            return new LIT(rb.toString(), getLocale(rb.getLang()));
        } else if (rb.getType() != null) {
            return new LIT(rb.toString(), getUID(rb.getType()));
        } else {
            return new LIT(rb.toString());
        }
    }

    private UID getUID(String datatype) {
        UID uid = datatypes.get(datatype);
        if (uid == null) {
            uid = new UID(datatype);
            datatypes.put(datatype, uid);
        }
        return uid;
    }

    private Locale getLocale(String lang) {
        Locale locale = locales.get(lang);
        if (locale == null) {
            locale = LocaleUtil.parseLocale(lang);
            locales.put(lang, locale);
        }
        return locale;
    }

}
