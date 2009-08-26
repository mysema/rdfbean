package com.mysema.rdf.demo.foaf;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

import freemarker.template.Configuration;

public class FreemarkerTest {

    private static Configuration configuration;
    
    @Test
    public void testFreemarkerAccess() throws Exception {
        
        configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File("src/test/resources/"));
        
        Map<String, Object> model = new HashMap<String, Object>();

        UID homepageUID = new UID("foaf", "homepage");
        UID workpageUID = new UID("foaf", "workpage");
        
        UID documentUID = new UID("foaf", "mypage");
        
        TestResource resource = new TestResource();

        TestProperty prop = new TestProperty<UID>(homepageUID);
        prop.getLiteralsSet().add(new LIT("http://www.koti.com"));
        resource.getPropertiesMap().put(homepageUID, prop);
        prop.getReferencesSet().add(documentUID);
        
        prop = new TestProperty<UID>(workpageUID);
        prop.getLiteralsSet().add(new LIT("http://www.mysema.com"));
        prop.getLiteralsSet().add(new LIT("http://www.mysema1.com"));
        resource.getPropertiesMap().put(workpageUID, prop);
        

        
        model.put("resource", resource);
        model.put("uid", homepageUID);
        
        Writer writer = new PrintWriter(System.out);
        configuration.getTemplate("freemarker_test.ftl").process(model, writer);
        //writer.close();
    }
}
