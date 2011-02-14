package com.mysema.rdfbean.rdb;

import org.junit.Test;

import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QNODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFQuery;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

public class BooleanQueryTest extends AbstractConnectionTest{
    
    private static final QNODE<ID> subject = new QNODE<ID>(ID.class, "s");
    
    private static final QNODE<UID> predicate = new QNODE<UID>(UID.class, "p");
    
    private static final QNODE<NODE> object = new QNODE<NODE>(NODE.class, "o");
    
    private RDFQuery query(){
        return new RDFQueryImpl(connection);
    }     
    
    @Test
    public void Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .ask();
    }
    
    @Test
    public void Patterns_as_Group(){
        query().where(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)))
               .ask();
    }

}
