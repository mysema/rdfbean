package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.mysema.rdfbean.domains.ListDomain.Element;
import com.mysema.rdfbean.domains.ListDomain.Elements;
import com.mysema.rdfbean.domains.ListDomain.Identifiable;
import com.mysema.rdfbean.domains.ListDomain.LinkElement;
import com.mysema.rdfbean.domains.ListDomain.TextElement;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;

public class ListHandlingTest {
    
    @Test
    public void test(){
        Repository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, Identifiable.class, Elements.class, Element.class, LinkElement.class, TextElement.class);
        
        Elements elements = new Elements();
        elements.elements = Arrays.<Element>asList(new LinkElement(), new TextElement());
        session.save(elements);
        session.clear();
        
        Elements other = session.getById(elements.id, Elements.class);
        assertEquals(elements.elements.size(), other.elements.size());                        
    }

}
