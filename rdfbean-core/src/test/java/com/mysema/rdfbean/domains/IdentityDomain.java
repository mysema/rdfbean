/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface IdentityDomain {

    @ClassMapping
    public static class Entity1 {

        @Id(IDType.RESOURCE)
        public ID id;

        public ID getId() {
            return id;
        }

        @Override
        public String toString() {
            return id.toString();
        }

    }

    @ClassMapping
    public class Entity2 {

        @Id(IDType.LOCAL)
        public String id;

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

}
