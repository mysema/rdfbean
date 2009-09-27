package com.mysema.rdfbean.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openrdf.rio.RDFFormat;

import com.mysema.rdfbean.beangen.BeanGen;
import com.mysema.rdfbean.sesame.MemoryRepository;
import com.mysema.rdfbean.sesame.RDFSource;

/**
 * BeanGenMojo provides
 *
 * @author tiwe
 * @version $Id$
 * @goal beangen
 */
public class BeanGenMojo extends AbstractMojo{
    
    /**
     * @parameter
     */
    private File targetFolder;
    
    /**
     * @parameter
     */
    private File schemaFile;
    
    /**
     * @parameter
     */
    private String schemaNamespace;
    
    // TODO : namespace to package
    
    // TODO : namespace to prefix

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {        
        try {
            MemoryRepository repository = new MemoryRepository();
            repository.setSources(new RDFSource(schemaFile.toURL().toString(), RDFFormat.RDFXML, schemaNamespace));
            repository.initialize();
            
            BeanGen beanGen = new BeanGen(repository);
            beanGen.addExportNamespace(schemaNamespace);
            // TODO : more
            beanGen.handleRDFSchema(targetFolder.getAbsolutePath());
        } catch (MalformedURLException e) {
            String error = "Caught " + e.getClass().getName();
            getLog().error(error);
            throw new MojoExecutionException(error, e);
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            getLog().error(error);
            throw new MojoExecutionException(error, e);
        }        
        
    }

}
