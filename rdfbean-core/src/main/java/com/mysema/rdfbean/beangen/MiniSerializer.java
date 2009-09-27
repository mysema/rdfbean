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
import com.mysema.util.SerializerHelper;

/**
 * DefaultSerializer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MiniSerializer extends DefaultSerializer{
    
    @Override
    public void serialize(BeanType model, Writer writer) throws IOException {
       printPackage(model.getPackageName(), writer);
       openClass(model, model.getSuperTypes(), writer);
       for (Property property : model.getProperties()){
           propertyField(model, property, writer);
       }
       closeBlock(writer);
    }

    @Override
    public void serialize(EnumType model, Writer writer) throws IOException {
        printPackage(model.getPackageName(), writer);
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
    
}
