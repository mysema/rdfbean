/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.beans.PropertyEditorSupport;

import com.google.common.base.Strings;

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
        if (Strings.isNullOrEmpty(text)) {
            setValue(null);
        } else {
            setValue(new UID(text));
        }
    }

}
