/**
 *
 */
package com.mysema.rdfbean.object;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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

    @Nullable
    private List<String> parameters;

    private int counter = 0;

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        close();
        inConstructor = name.equals("<init>");
        if (inConstructor) {
            counter = 0;
            parameters = new ArrayList<String>();
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (inConstructor && index >= counter) {
            if (!name.equals("this")) {
                parameters.add(name);
            }
            counter = index + 1;
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void close() {
        if (inConstructor && !parameters.isEmpty()) {
            constructors.add(parameters);
            parameters = null;
        }
        inConstructor = false;
    }

    public List<List<String>> getConstructors() {
        return constructors;
    }

}