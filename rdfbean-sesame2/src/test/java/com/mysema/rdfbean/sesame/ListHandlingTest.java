package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.Job;
import com.mysema.rdfbean.domains.JobItem;
import com.mysema.rdfbean.domains.ListDomain.Element;
import com.mysema.rdfbean.domains.ListDomain.Elements;
import com.mysema.rdfbean.domains.ListDomain.Identifiable;
import com.mysema.rdfbean.domains.ListDomain.LinkElement;
import com.mysema.rdfbean.domains.ListDomain.TextElement;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Element.class, Elements.class, Identifiable.class, LinkElement.class, TextElement.class })
public class ListHandlingTest extends SessionTestBase {

    @Test
    public void test() {
        Elements elements = new Elements();
        elements.elements = Arrays.<Element> asList(new LinkElement(), new TextElement());
        session.save(elements);
        session.clear();

        Elements other = session.getById(elements.id, Elements.class);
        assertTrue(elements != other);
        assertEquals(elements.elements.size(), other.elements.size());
        assertNotNull(other.elements.get(0));
        assertNotNull(other.elements.get(1));
    }

    @Test
    public void test2() throws IOException {
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(new RDFSource("classpath:/list_test.ttl", Format.TURTLE, TEST.NS)); 
        repository.initialize();
        Session privateSession = SessionUtil.openSession(repository, Job.class, JobItem.class);
        Job job = privateSession.get(Job.class, new UID("http://www.foo.com/Job/1"));
        privateSession.close();

        assertEquals(job.getJobItems().size(), 1);
    }

}
