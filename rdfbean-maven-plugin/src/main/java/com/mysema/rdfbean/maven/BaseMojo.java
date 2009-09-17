package com.mysema.rdfbean.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * BaseMojo provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class BaseMojo extends AbstractMojo{
    
    /** @parameter expression="${project}" readonly=true required=true */
    private MavenProject project;
    
    protected URLClassLoader getProjectClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classpathElements = project.getCompileClasspathElements();
        List<URL> urls = new ArrayList<URL>(classpathElements.size());
        for (String element : classpathElements){
            File file = new File(element);
            if (file.exists()){
                urls.add(file.toURL());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());       
    }

}
