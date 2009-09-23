package com.mysema.rdfbean.beangen;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.schema.BeanGen;
import com.mysema.rdfbean.sesame.FOAF;
import com.mysema.rdfbean.sesame.MemoryRepository;
import com.mysema.rdfbean.sesame.RDFSource;

/**
 * BeanGenTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanGenTest{
    
    private static final String BLOG_NS = "http://www.mysema.com/semantics/blog/#";
    
    private static final String WINE_NS = "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#";
    
//    private String targetFolder = "target/generated-test-sources/java";
    private String targetFolder = "target";
    
    @Test
    public void foaf() throws StoreException, ClassNotFoundException{
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(new RDFSource("classpath:/foaf.rdf", RDFFormat.RDFXML, FOAF.NS));
        repository.initialize();
        
        BeanGen beanGen = new BeanGen(repository);
        beanGen.addExportNamespace(FOAF.NS);
        beanGen.addNamespace(FOAF.NS, "foaf");
        beanGen.addNamespace(OWL.NS, "owl");
        beanGen.addNamespace("http://xmlns.com/wordnet/1.6/", "wordnet");
        beanGen.addNamespace("http://www.w3.org/2000/10/swap/pim/contact#", "swap");
        beanGen.addNamespace("http://www.w3.org/2003/01/geo/wgs84_pos#", "geo");
        beanGen.handleRDFSchema(targetFolder);
    }
    
    @Test
    public void wine(){
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(new RDFSource("classpath:/wine.owl", RDFFormat.RDFXML, WINE_NS));
        repository.initialize();
        
        BeanGen beanGen = new BeanGen(repository);
        beanGen.addExportNamespace(WINE_NS);
        beanGen.addNamespace(WINE_NS, "wine");
        beanGen.addNamespace("http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#", "food");
        beanGen.addNamespace("http://www.w3.org/2001/XMLSchema#", "xsd");
        beanGen.handleOWL(targetFolder);
    }
    
    @Test
    @Ignore
    public void blog(){
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(new RDFSource("classpath:/blog.owl", RDFFormat.RDFXML, BLOG_NS));
        repository.initialize();
        
        BeanGen beanGen = new BeanGen(repository);
        beanGen.addExportNamespace(BLOG_NS);
        beanGen.addNamespace(BLOG_NS, "blog");
        beanGen.handleOWL(targetFolder);
    }

}
