package com.mysema.rdfbean.sesame.domains;

import java.util.List;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

@ClassMapping(ns = "http://www.foo.com/foo.owl#")
public class Job {

    @Id(value = IDType.RESOURCE)
    private ID uri;

    @Predicate
    private String jobId;

    @Predicate
    private List<JobItem> jobItem;

    public Job() {
    }

    public String getJobId() {
        return jobId;
    }

    public ID getUri() {
        return uri;
    }

    public void setUri(ID uri) {
        this.uri = uri;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setJobItems(List<JobItem> jobItems) {
        this.jobItem = jobItems;
    }

    public List<JobItem> getJobItems() {
        return jobItem;
    }

}
