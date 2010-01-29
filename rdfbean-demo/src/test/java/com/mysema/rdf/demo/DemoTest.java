package com.mysema.rdf.demo;

import static com.mysema.rdf.demo.domain.QCompany.company;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mysema.rdf.demo.domain.Company;
import com.mysema.rdf.demo.domain.Person;
import com.mysema.rdf.demo.domain.QPerson;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/persistence.xml" })
public class DemoTest {

    private Session session;

    @Autowired
    private SessionFactory sessionFactory;
    
    private void closeSession() throws IOException {
        if (this.session != null) {
            this.session.close();
        }
    }

    @Test
    public void demonstrate() throws IOException {
        Person person = new Person("John", "Doe");
        person.setAge(15);
        newSession();
        session.save(person);

        newSession();
        Person p = session.findInstances(Person.class).get(0);
        assertNotSame(person, p);
        assertEquals("John", p.getFirstName());
        assertEquals(15, p.getAge());

        QPerson personVar = new QPerson("person");
        assertEquals(1, session.from(personVar)
                .where(personVar.displayName.startsWith("J")
                        .and(personVar.age.gt(14))).list(personVar).size());

        Company company = new Company();
        company.setOfficialName("Da Company");
        company.addEmployee(person);
        session.save(company);

        newSession();
        List<Object> objects = session.findInstances(Object.class);
        assertEquals(3, objects.size()); // John Doe, Da Company and Mysema

        objects = session.findInstances(Object.class, new UID(DEMO.NS, "Company"));
        assertEquals(2, objects.size()); // Da Company and Mysema
    }
    
    @Test
    public void findMysema() throws IOException {
        newSession();
        List<Company> companies = session
        .from(company)
        .where(company.displayName.eq("Mysema"))
        .list(company);
        
        assertEquals(1, companies.size());
        Company mysema = companies.get(0);
        assertEquals("Mysema Oy", mysema.getOfficialName());
    }

    private void newSession() throws IOException {
        closeSession();
        this.session = sessionFactory.openSession();
    }

    @After
    public void tearDown() throws IOException{
        closeSession();
    }
}
