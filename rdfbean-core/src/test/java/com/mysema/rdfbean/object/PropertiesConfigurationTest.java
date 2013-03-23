package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.PropertiesDomain.InvalidProject1;
import com.mysema.rdfbean.domains.PropertiesDomain.InvalidProject2;
import com.mysema.rdfbean.domains.PropertiesDomain.InvalidProject3;
import com.mysema.rdfbean.domains.PropertiesDomain.Person;
import com.mysema.rdfbean.domains.PropertiesDomain.Project;
import com.mysema.rdfbean.model.UID;

public class PropertiesConfigurationTest {

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new DefaultConfiguration(TEST.NS, Project.class, Person.class);
    }

    @Test
    public void MappedClass() throws SecurityException,
            NoSuchFieldException {

        Field field = Project.class.getDeclaredField("infos");

        assertNotNull(field);

        MappedClass mappedClass = configuration.getMappedClass(Project.class);
        boolean containsInfos = false;

        for (MappedProperty<?> property : mappedClass.getDynamicProperties()) {
            if (property.isDynamic() && property.getName().equals("infos")) {
                containsInfos = true;
                assertEquals(UID.class, property.getKeyType());
                assertTrue(Set.class.isAssignableFrom(property.getDynamicCollectionType()));
                assertEquals(String.class, property.getDynamicCollectionComponentType());
            }
        }

        assertTrue("Could not find property 'infos'", containsInfos);
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void InvalidProject1() {
        // TODO How to handle this case
        configuration.getMappedClass(InvalidProject1.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void InvalidProject2() {
        new DefaultConfiguration(InvalidProject2.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void InvalidProject3() {
        new DefaultConfiguration(InvalidProject3.class);
    }

}
