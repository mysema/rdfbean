package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface ResourceDomain {
    
    @ClassMapping(ns=TEST.NS)
    public class Resource {
        
        @Id(IDType.RESOURCE)
        private ID id;

        public ID getId() {
            return id;
        }
        
    }

}
