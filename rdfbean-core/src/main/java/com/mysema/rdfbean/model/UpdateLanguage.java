package com.mysema.rdfbean.model;

public final class UpdateLanguage<D,Q> {
    
    public static final UpdateLanguage<String, RDFUpdate> SPARQL_UPDATE = new UpdateLanguage<String, RDFUpdate>("SPARQL_UPDATE"); 
    
    private final String name;
    
    private UpdateLanguage(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }

}
