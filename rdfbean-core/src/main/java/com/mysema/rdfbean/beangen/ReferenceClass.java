/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.owl.OWLClass;

/**
 * @author sasa
 *
 */
@ClassMapping
public class ReferenceClass extends OWLClass {

    public ReferenceClass(ID id) {
        super(id);
    }

}
