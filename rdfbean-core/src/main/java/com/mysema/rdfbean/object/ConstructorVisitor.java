/**
 * 
 */
package com.mysema.rdfbean.object;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * @author tiwe
 *
 */
public class ConstructorVisitor extends EmptyVisitor {
    
    private boolean inConstructor = false;
    
    private final List<List<String>> constructors = new ArrayList<List<String>>();
    
    private List<String> parameters; 

    private int counter = 0;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
        inConstructor = name.equals("<init>");
        if (inConstructor){
            counter = 0;
            parameters = new ArrayList<String>();
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){
        if (inConstructor && counter == index){
            if (!name.equals("this")){
                parameters.add(name);    
            }            
            counter++;
        }                
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitEnd(){
        if (inConstructor && !parameters.isEmpty()){
            constructors.add(parameters);
            parameters = null;
        }
        inConstructor = false;
        super.visitEnd();
    }

    public List<List<String>> getConstructors() {
        return constructors;
    }

    
    
}