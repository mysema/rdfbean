package com.mysema.rdfbean.beangen;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.codegen.EntityType;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.rdfs.RDFSClass;


public class JavaBeanExporterTest extends AbstractExportTest{
    
    private JavaBeanExporter exporter;
    
    @Before
    public void setUp(){
        exporter = new JavaBeanExporter(true);
        exporter.addPackage("http://www.mysema.com/semantics/blog/#", "com.mysema.blog");
        exporter.addPackage("http://purl.org/dc/elements/1.1/", "com.mysema.dc");
        exporter.addPackage("http://purl.org/dc/terms/", "com.mysema.dc");
        exporter.addPackage("http://www.mysema.com/rdfbean/demo#", "com.mysema.demo");
        exporter.addPackage("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#", "com.mysema.wine");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void createBeanType(){                
        List<RDFSClass> rdfTypes = session.findInstances(RDFSClass.class);
        assertFalse(rdfTypes.isEmpty());
        for (RDFSClass<?> rdfType : rdfTypes){
            if (rdfType.getId().isBNode()){
                continue;
            }
            EntityType entityType = exporter.createBeanType(rdfType);
            
            // supertype count
            int supertypes = 0;
            for (RDFSClass<?> superClass : rdfType.getSuperClasses()){
                if (superClass != null && !superClass.getClass().equals(Restriction.class)){
                    supertypes++;
                }
            }
            assertEquals(supertypes, entityType.getSuperTypes().size());
            
            // property count
            assertTrue(entityType.getProperties().size() >= rdfType.getProperties().size());
           
        }
    }

}
