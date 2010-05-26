package com.mysema.rdfbean.tapestry;

import java.lang.annotation.Annotation;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;

import com.mysema.rdfbean.sesame.query.ComplexPathsTest.NoteRevision;

public class SimplePropertyModel implements PropertyModel {
    
    private final String propertyName;
    
    private final Class<?> propertyType;
    
    public SimplePropertyModel(String name, Class<?> type) {
        this.propertyName = name;
        this.propertyType = type;
    }

    @Override
    public PropertyModel dataType(String dataType) {
        return null;
    }

    @Override
    public PropertyConduit getConduit() {
        return null;
    }

    @Override
    public String getDataType() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public Class<?> getPropertyType() {
        return propertyType;
    }

    @Override
    public boolean isSortable() {
        return true;
    }

    @Override
    public PropertyModel label(String label) {
        return null;
    }

    @Override
    public BeanModel<NoteRevision> model() {
        return null;
    }

    @Override
    public PropertyModel sortable(boolean sortable) {
        return null;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

}