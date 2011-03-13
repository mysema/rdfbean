package com.mysema.rdfbean.beangen;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSDatatype;
import com.mysema.rdfbean.rdfs.RDFSResource;


public class JavaBeanExporterTest{
        
    UID class1 = new UID(TEST.NS, "Class1");
    UID class2 = new UID(TEST.NS, "Class2");
    UID prop1 = new UID(TEST.NS, "prop1");
    UID prop2 = new UID(TEST.NS, "prop2");
    UID prop3 = new UID(TEST.NS, "prop2");
        
    Session session;
    
    @Before
    public void setUp(){
        Repository repository = new MiniRepository();
        session = SessionUtil.openSession(repository, RDFSClass.class.getPackage(), OWLClass.class.getPackage());
        RDFSClass<?> xsdString = new RDFSDatatype(XSD.stringType);
        
        // classes
        OWLClass cl1 = new OWLClass(class1);
        OWLClass cl2 = new OWLClass(class2);
        cl2.addSuperClass(cl1);
        cl2.setOneOf(Arrays.asList(new RDFSResource(new UID(TEST.NS, "res1")), new RDFSResource(new UID(TEST.NS, "res2"))));
        
        // properties
        RDFProperty p1 = new RDFProperty(prop1).addDomain(cl1).addRange(xsdString);
        RDFProperty p2 = new RDFProperty(prop2).addDomain(cl1).addRange(cl2);
        RDFProperty p3 = new RDFProperty(prop3);
        
        // restrictions
        Restriction res = new Restriction();        
        res.setOnProperty(p3);
        res.setCardinality(2);
        res.setSomeValuesFrom(xsdString);
        assertTrue(res.isDefined());
        cl1.addSuperClass(res);
        
        session.saveAll(cl1, cl2, p1, p2, p3, res);
        session.flush();
        session.clear();
    }
    
    @Test
    public void Export() throws IOException{
        JavaBeanExporter exporter = new JavaBeanExporter(true);
        exporter.addPackage(TEST.NS, "com.example");
        exporter.export(session, new File("target/beanExport"));
        
        assertTrue(new File("target/beanExport/com/example/Class1.java").exists());
        assertTrue(new File("target/beanExport/com/example/Class1.java").exists());
        
    }

}
