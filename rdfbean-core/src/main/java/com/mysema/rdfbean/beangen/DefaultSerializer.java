/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.RDF;
import com.mysema.util.SerializerHelper;

/**
 * DefaultSerializer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultSerializer extends SerializerHelper implements Serializer {
    
    @Override
    public void serialize(BeanType model, Writer writer) throws IOException {
       printPackage(model.getPackageName(), writer);
       nl(writer);
       
       printImports(writer);
       nl(writer);
       
       openClass(model, model.getSuperTypes(), writer);
       nl(writer);
       for (Property property : model.getProperties()){
           propertyField(model, property, writer);
           nl(writer);
       }
       for (Property property : model.getProperties()){
           propertyGetter(model, property, writer);
           nl(writer);
           propertySetter(model, property, writer);
           nl(writer);
       }
       closeBlock(writer);
    }

    @Override
    public void serialize(EnumType model, Writer writer) throws IOException {
        printPackage(model.getPackageName(), writer);
        nl(writer);
        
        printImports(writer);
        nl(writer);
        
        openEnum(model, writer);
        for (int i = 0; i < model.getEnums().size(); i++){
            writer.append("    " + model.getEnums().get(i));
            if (i < model.getEnums().size() - 1){
                writer.append(",\n");
            }else{
                nl(writer);
            }
        }
        closeBlock(writer);
    }

    private void printImports(Writer writer) throws IOException {
        importPackage(Predicate.class.getPackage().getName(), writer);                
    }
    
    protected void classMapping(Type type, Writer writer) throws IOException{
        StringBuilder builder = new StringBuilder();
        builder.append("@ClassMapping(ns=\"").append(type.getRdfType().getNamespace());
        if (!type.getSimpleName().equals(type.getRdfType().getLocalName())){
            builder.append("\",ln=\"").append(type.getRdfType().getLocalName());
        }
        builder.append("\")\n");
        writer.append(builder.toString());
    }

    protected void openEnum(EnumType type, Writer writer) throws IOException {
        classMapping(type, writer);
        StringBuilder builder = new StringBuilder();
        builder.append("public enum ").append(type.getSimpleName()).append(" {\n");
        writer.append(builder.toString());
    }
    
    protected void openClass(Type type, List<Type> superTypes, Writer writer) throws IOException {
        classMapping(type, writer);
        StringBuilder builder = new StringBuilder();        
        builder.append("public class ").append(type.getSimpleName());
        if (!superTypes.isEmpty()){
            Type superType = superTypes.get(0);
            builder.append(" extends ");
            if (!superType.getPackageName().equals(type.getPackageName())){
                builder.append(superType.getPackageName()).append(".");
            }
            builder.append(superType.getSimpleName());
        }
        builder.append(" {\n");
        writer.append(builder.toString());        
    }
    
    private String getType(Type type, Type context){
        if (type.getPackageName().equals("java.lang") || type.getPackageName().equals(context.getPackageName())){
            return type.getSimpleName();
        }else{
            return type.getPackageName() + "." + type.getSimpleName();
        }
    }
    
    protected void propertyField(Type model, Property property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String ns = null; String ln = null;
        if (!property.getRdfProperty().getNamespace().equals(model.getRdfType().getNamespace())){
            ns = property.getRdfProperty().getNamespace();
        }
        if (!property.getName().equals(property.getRdfProperty().getLocalName())){
            ln = property.getRdfProperty().getLocalName();
        }
        builder.append(getIndent() + "@Predicate");
        if (ns != null || ln != null){
            builder.append("(");
            if (ns != null){
                builder.append("ns=\"").append(ns).append("\"");
                if (ln != null) builder.append(",");
            }
            if (ln != null){
                builder.append("ln=\"").append(ln).append("\"");
            }
            builder.append(")");
        }
        builder.append("\n");
        if (property.getRdfProperty().equals(RDF.text)){
            // TODO : handle Map<Locale,String> type localization
            builder.append("@Localized\n");
        }
        builder.append(getIndent() + "private ");
        builder.append(getType(property.getType(),model)).append(" ");
        builder.append(property.getName()).append(";\n");
        writer.append(builder.toString());
    }
    
    protected void propertyGetter(Type model, Property property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append(getIndent() + "public ").append(type).append(" ");
        builder.append("get" + StringUtils.capitalize(property.getName())).append("(){\n");
        builder.append(getIndent() + getIndent() + "return ").append(property.getName()).append(";\n");
        builder.append(getIndent() + "}\n");
        writer.append(builder.toString());
    }
    
    protected void propertySetter(Type model, Property property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append(getIndent() + "public void set" + StringUtils.capitalize(property.getName()));
        builder.append("(").append(type).append(" ").append(property.getName()).append("){\n");
        builder.append(getIndent() + getIndent() + "this.").append(property.getName()).append(" = ").append(property.getName()).append(";\n");
        builder.append(getIndent() + "}\n");
        writer.append(builder.toString());
    }
    
}
