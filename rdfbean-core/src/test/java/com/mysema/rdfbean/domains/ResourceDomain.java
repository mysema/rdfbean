/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;

public interface ResourceDomain {

    @ClassMapping
    public class Resource {

        @Id
        private ID id;

        public ID getId() {
            return id;
        }

    }

}
