package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

@ClassMapping(ns = "http://www.foo.com/foo.owl#")
public class JobItem {

    @Id(value = IDType.RESOURCE)
    private ID id;

    public JobItem() {
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

} 