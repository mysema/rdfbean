package com.mysema.rdf.demo.foaf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import com.mysema.rdf.demo.generic.Property;
import com.mysema.rdf.demo.generic.Value;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

public class TestProperty<T> implements Property<T> {

    private Collection<LIT> literals = new HashSet<LIT>();
    
    private Collection<T> references = new HashSet<T>();
    
    private UID uid;
    
    public TestProperty(UID uid) {
        this.uid = uid;
    }
    
    @Override
    public void add(LIT value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void add(Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public LIT getLiteral() {
        return literals.iterator().next();
    }

    @Override
    public LIT getLiteral(Locale locale) {
        return literals.iterator().next();
    }

    @Override
    public Collection<LIT> getLiterals() {
        return literals;
    }

    @Override
    public T getReference() {
        return references.iterator().next();
    }

    @Override
    public Collection<T> getReferences() {
        return references;
    }

    @Override
    public UID getId() {
        return uid;
    }

    @Override
    public Value<T> getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getValueCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Collection<Value<T>> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(LIT value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void remove(Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAll() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLiteral(LIT value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLiterals(Collection<LIT> values) {
       literals = values;
    }

    public Collection<LIT> getLiteralsSet() {
        return literals;
    }
    
    @Override
    public void setReference(Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setReferences(Collection<T> values) {
        // TODO Auto-generated method stub
        
    }

    public Collection<T> getReferencesSet() {
        return references;
    }
    
}
