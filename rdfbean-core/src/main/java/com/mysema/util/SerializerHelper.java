/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.io.IOException;
import java.io.Writer;

/**
 * SerializerBase provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class SerializerHelper {
    
    private String indent = "    ";
    
    protected String getIndent(){
        return indent;
    }

    protected void closeBlock(Writer writer) throws IOException {
        writer.append("}\n");        
    }

    protected void nl(Writer writer) throws IOException {
        writer.append("\n");
    }

    protected void printPackage(String packageName, Writer writer) throws IOException {
        writer.append("package " + packageName + ";\n");        
    }
    
    protected void importPackage(String packageName, Writer writer) throws IOException{
        writer.append("import " + packageName +".*;\n");
    }
    
    protected void importClass(String className, Writer writer) throws IOException{
        writer.append("import " + className +";\n");
    }

}
