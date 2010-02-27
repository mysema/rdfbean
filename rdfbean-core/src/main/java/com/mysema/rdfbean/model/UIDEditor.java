/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;

/**
 * @author sasa
 *
 */
public class UIDEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        UID uid = (UID) getValue();
        if (uid == null) {
            return "";
        } else {
            return uid.getId();
        }
    }

    @Override
    public void setAsText(String text) {
        if (StringUtils.isEmpty(text)) {
            setValue(null);
        } else {
            setValue(new UID(text));
        }
    }

}
