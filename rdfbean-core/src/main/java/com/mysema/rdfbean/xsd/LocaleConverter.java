package com.mysema.rdfbean.xsd;

import java.util.Locale;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.converters.Converter;

public class LocaleConverter implements Converter<Locale> {

    @Override
    public Locale fromString(String str) {
        return LocaleUtil.parseLocale(str);
    }

    @Override
    public Class<Locale> getJavaType() {
        return Locale.class;
    }

    @Override
    public String toString(Locale locale) {
        return LocaleUtil.toLang(locale);
    }

}
