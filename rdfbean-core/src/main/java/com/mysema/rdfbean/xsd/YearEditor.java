/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;

/**
 * @author sasa
 *
 */
public class YearEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        Year year = (Year) getValue();
        if (year == null) {
            return "";
        } else {
            return Integer.toString(year.getYear());
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)) {
            setValue(null);
        } else {
            setValue(new Year(Integer.parseInt(text)));
        }
    }

}
