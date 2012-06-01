/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.beans.PropertyEditorSupport;

import com.google.common.base.Strings;

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
    public void setAsText(String text) {
        if (Strings.isNullOrEmpty(text)) {
            setValue(null);
        } else {
            setValue(new Year(Integer.parseInt(text)));
        }
    }

}
