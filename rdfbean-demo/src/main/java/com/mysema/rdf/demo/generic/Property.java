package com.mysema.rdf.demo.generic;

import java.util.Collection;
import java.util.Locale;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

public interface Property<T> {

    void add(LIT value);

    void add(T value);

    LIT getLiteral();

    LIT getLiteral(Locale locale);

    Value<T> getValue();

    Collection<Value<T>> getValues();

    Collection<LIT> getLiterals();

    void setLiterals(Collection<LIT> values);

    void setReferences(Collection<T> values);

    T getReference();

    Collection<T> getReferences();

    UID getId();

    int getValueCount();

    void remove(LIT value);

    void remove(T value);

    void removeAll();

    void setLiteral(LIT value);

    void setReference(T value);
}
