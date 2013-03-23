/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import java.util.List;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

public interface ListDomain {

    @ClassMapping
    public class Identifiable {
        @Id(IDType.LOCAL)
        public String id;

    }

    @ClassMapping
    public class Elements extends Identifiable {

        @Predicate
        public List<Element> elements;

    }

    @ClassMapping
    public interface Element {

    }

    @ClassMapping
    public class LinkElement extends Identifiable implements Element {

        @Predicate
        public String url;
    }

    @ClassMapping
    public class TextElement extends Identifiable implements Element {

        @Predicate
        public String text;
    }

}
