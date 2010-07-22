package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.mysema.rdfbean.domains.ListDomain.Element;
import com.mysema.rdfbean.domains.ListDomain.Elements;
import com.mysema.rdfbean.domains.ListDomain.Identifiable;
import com.mysema.rdfbean.domains.ListDomain.LinkElement;
import com.mysema.rdfbean.domains.ListDomain.TextElement;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Element.class, Elements.class, Identifiable.class, LinkElement.class, TextElement.class})
public class ListHandlingTest extends AbstractRDBTest {
    
    @Test
    public void test(){        
        Elements elements = new Elements();
        elements.elements = Arrays.<Element>asList(new LinkElement(), new TextElement());
        session.save(elements);
        session.clear();
        
        Elements other = session.getById(elements.id, Elements.class);
        assertTrue(elements != other);
        assertEquals(elements.elements.size(), other.elements.size());                        
    }

}