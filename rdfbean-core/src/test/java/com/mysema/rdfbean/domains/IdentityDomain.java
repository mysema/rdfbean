package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface IdentityDomain {
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity1{
        
        @Id(IDType.RESOURCE)
        public ID id;

        public ID getId() {
            return id;
        }
        
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Entity2{
        
        @Id(IDType.LOCAL)
        public String id;

        public String getId() {
            return id;
        }
        
        
    }

}
