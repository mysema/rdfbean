package com.mysema.rdfbean.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaCompiler;

import junit.framework.Assert;

import com.mysema.codegen.SimpleCompiler;
import com.mysema.util.FileUtils;

public abstract class AbstractProcessorTest {

    protected void process(Class<? extends AbstractProcessor> processorClass, List<String> classes, String target) throws IOException{
        File out = new File("target/" + target);
        FileUtils.delete(out);
        if (!out.mkdirs()){
            Assert.fail("Creation of " + out.getPath() + " failed");
        }
        compile(processorClass, classes, target);
    } 
    
    protected void compile(Class<? extends AbstractProcessor> processorClass, List<String> classes, String target) throws IOException{
        JavaCompiler compiler = new SimpleCompiler();
        System.out.println(compiler.getClass().getName());
        List<String> options = new ArrayList<String>(classes.size() + 3);
        options.add("-s");
        options.add("target/" + target);
        options.add("-proc:only");
        options.add("-processor");
        options.add(processorClass.getName());
        options.add("-sourcepath");
        options.add("src/test/java");
        options.addAll(classes);
        int compilationResult = compiler.run(null, System.out, System.err, options.toArray(new String[options.size()]));

        if(compilationResult == 0){
            System.out.println("Compilation is successful");
        }else{
            Assert.fail("Compilation Failed");
        }
        
    }
    

}
