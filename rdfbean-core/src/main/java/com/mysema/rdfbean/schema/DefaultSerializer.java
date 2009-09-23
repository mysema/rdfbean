package com.mysema.rdfbean.schema;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.Predicate;

/**
 * DefaultSerializer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultSerializer implements Serializer {

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
       close(writer);
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
        close(writer);
    }

    private void printImports(Writer writer) throws IOException {
        writer.append("import " + Predicate.class.getPackage().getName() +".*;\n");        
    }

    private void close(Writer writer) throws IOException {
        writer.append("}\n");        
    }

    protected void nl(Writer writer) throws IOException {
        writer.append("\n");
    }

    protected void printPackage(String packageName, Writer writer) throws IOException {
        writer.append("package " + packageName + ";\n");        
    }

    protected void openEnum(EnumModel model, Writer writer) throws IOException {
        writer.append("@ClassMapping(ns=\"\")\n");
        writer.append("public enum " + model.getSimpleName() + " {\n");
    }
    
    private String getType(TypeModel type, TypeModel context){
        if (type.getPackageName().equals("java.lang") || type.getPackageName().equals(context.getPackageName())){
            return type.getSimpleName();
        }else{
            return type.getPackageName()+"."+type.getSimpleName();
        }
    }
    
    protected void propertyField(TypeModel model, PropertyModel property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (property.getRdfProperty().getNamespace().equals(model.getRdfType().getNamespace())){
            builder.append("    @Predicate\n");
        }else{
            // TODO
        }
        builder.append("    private ");
        builder.append(getType(property.getType(),model)).append(" ");
        builder.append(property.getName()).append(";\n");
        writer.append(builder.toString());
    }
    
    protected void propertyGetter(TypeModel model, PropertyModel property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append("    public ").append(type).append(" ");
        builder.append("get" + StringUtils.capitalize(property.getName())).append("(){\n");
        builder.append("        return ").append(property.getName()).append(";\n");
        builder.append("    }\n");
        writer.append(builder.toString());
    }
    
    protected void propertySetter(TypeModel model, PropertyModel property, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        String type = getType(property.getType(), model);
        builder.append("    public void set" + StringUtils.capitalize(property.getName()));
        builder.append("(").append(type).append(" ").append(property.getName()).append("){\n");
        builder.append("        this.").append(property.getName()).append(" = ").append(property.getName()).append(";\n");
        builder.append("    }\n");
        writer.append(builder.toString());
    }
    
    protected void openClass(TypeModel type, List<TypeModel> superTypes, Writer writer) throws IOException {
        StringBuilder builder = new StringBuilder();
        writer.append("@ClassMapping(ns=\"\")\n");
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

    @Override
    public void setUsePrimitives(boolean usePrimitives) {
        this.usePrimitives = usePrimitives;        
    }

}
