/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame.object;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.sesame.MemoryRepository;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.sesame.domains.Job;
import com.mysema.rdfbean.sesame.domains.JobItem;

public class ListHandlingTest extends SessionTestBase {

    @Test
    public void test() throws IOException {
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(new RDFSource("classpath:/list_test.trig", Format.TRIG, TEST.NS));
        repository.initialize();
        Session privateSession = SessionUtil.openSession(repository, Job.class, JobItem.class);
        Job job = privateSession.get(Job.class, new UID("http://www.foo.com/Job/1"));
        privateSession.close();

        assertEquals(job.getJobItems().size(), 1);
    }

}
