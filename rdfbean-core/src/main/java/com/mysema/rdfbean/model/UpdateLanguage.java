package com.mysema.rdfbean.model;

/**
 * @author tiwe
 * 
 * @param <D>
 * @param <Q>
 */
public final class UpdateLanguage<D, Q> {

    public static final UpdateLanguage<String, SPARQLUpdate> SPARQL_UPDATE = new UpdateLanguage<String, SPARQLUpdate>("SPARQL_UPDATE");

    private final String name;

    private UpdateLanguage(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
