package com.mysema.rdfbean.model;

/**
 * @author tiwe
 * 
 */
public interface RDFUpdate {

    RDFUpdate delete(PatternBlock... patterns);

    RDFUpdate from(UID... uid);

    RDFUpdate insert(PatternBlock... patterns);

    RDFUpdate into(UID... uid);

    RDFUpdate where(Block... blocks);

    void execute();

}
