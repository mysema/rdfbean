/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import java.util.List;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

public interface ListDomain {
    
    @ClassMapping(ns=TEST.NS)
    public class Identifiable {
        @Id(IDType.LOCAL)
        public String id;
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Elements extends Identifiable{
     
        @Predicate
        public List<Element> elements;
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public interface Element {
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class LinkElement extends Identifiable implements Element{
        
        @Predicate
        public String url;
    }
    
    @ClassMapping(ns=TEST.NS)
    public class TextElement extends Identifiable implements Element{
        
        @Predicate
        public String text;
    }

}
