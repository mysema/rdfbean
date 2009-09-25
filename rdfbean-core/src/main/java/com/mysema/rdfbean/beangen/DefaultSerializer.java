package com.mysema.rdfbean.beangen;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.util.SerializerHelper;

/**
 * DefaultSerializer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultSerializer extends SerializerHelper implements Serializer {

    private boolean usePrimitives;
    
    @Override
    public void serialize(BeanModel model, Writer writer) throws IOException {
       printPackage(model.getPackageName(), writer);
       nl(writer);
       
       printImports(writer);
       nl(writer);
       
       openClass(model, model.getSuperTypes(), writer);
       nl(writer);
       for (PropertyModel property : model.getProperties()){
           propertyField(model, property, writer);
           nl(writer);
       }
       for (PropertyModel property : model.getProperties()){
           propertyGetter(model, property, writer);
           nl(writer);
           propertySetter(model, property, writer);
           nl(writer);
       }
       closeBlock(writer);
    }

    @Override
    public void serialize(EnumModel model, Writer writer) throws IOException {
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

    protected void openEnum(EnumModel model, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("@ClassMapping(ns=\"").append(model.getRdfType().getNamespace()).append("\")\n");
        builder.append("public enum ").append(model.getSimpleName()).append(" {\n");
        writer.append(builder.toString());
    }
    
    protected void openClass(TypeModel type, List<TypeModel> superTypes, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("@ClassMapping(ns=\"").append(type.getRdfType().getNamespace()).append("\")\n");
        builder.append("public class ").append(type.getSimpleName());
        if (!superTypes.isEmpty()){
            TypeModel superType = superTypes.get(0);
            builder.append(" extends ");
            if (!superType.getPackageName().equals(type.getPackageName())){
                builder.append(superType.getPackageName()).append(".");
            }
            builder.append(superType.getSimpleName());
        }
        builder.append(" {\n");
        writer.append(builder.toString());
        
    }
    
    private String getType(TypeModel type, TypeModel context){
        return getType(type.getPackageName(), type.getSimpleName(), context.getPackageName());
    }
    
    protected void propertyField(TypeModel model, PropertyModel property, Writer writer) throws IOException {
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
        builder.append(getIndent() + "private ");
        builder.append(getType(property.getType(),model)).append(" ");
        builder.append(property.getName()).append(";\n");
        writer.append(builder.toString());
    }
    
    protected void propertyGetter(TypeModel model, PropertyModel property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append(getIndent() + "public ").append(type).append(" ");
        builder.append("get" + StringUtils.capitalize(property.getName())).append("(){\n");
        builder.append(getIndent() + getIndent() + "return ").append(property.getName()).append(";\n");
        builder.append(getIndent() + "}\n");
        writer.append(builder.toString());
    }
    
    protected void propertySetter(TypeModel model, PropertyModel property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append(getIndent() + "public void set" + StringUtils.capitalize(property.getName()));
        builder.append("(").append(type).append(" ").append(property.getName()).append("){\n");
        builder.append(getIndent() + getIndent() + "this.").append(property.getName()).append(" = ").append(property.getName()).append(";\n");
        builder.append(getIndent() + "}\n");
        writer.append(builder.toString());
    }
    
    @Override
    public void setUsePrimitives(boolean usePrimitives) {
        this.usePrimitives = usePrimitives;        
    }

}
