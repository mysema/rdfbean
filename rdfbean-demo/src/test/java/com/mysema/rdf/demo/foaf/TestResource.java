package com.mysema.rdf.demo.foaf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.mysema.rdfbean.model.UID;

public class TestResource<T> implements Resource<T> {

    private Map<UID, Property<T>> properties = new HashMap<UID, Property<T>>();

    @Override
    public Collection<Property<T>> getProperties() {
	return properties.values();
    }

    @Override
    public Property<T> getProperty(UID uid) {
	return properties.get(uid);
    }

    public void setProperties(Map<UID, Property<T>> properties) {
	this.properties = properties;
    }

    public Map<UID, Property<T>> getPropertiesMap() {
	return properties;
    }
}
