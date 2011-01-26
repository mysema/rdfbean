package com.mysema.rdfbean.jena;

import org.junit.Test;

import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFQuery;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

public class GraphQueryTest extends AbstractConnectionTest{
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");
    
    private RDFQuery query(){
        return new RDFQueryImpl(connection);
    }    
    
    @Test
    public void Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(Blocks.pattern(subject, predicate, object));
    }
    
    @Test
    public void Patterns_as_Group(){
        query().where(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)))
               .construct(Blocks.pattern(subject, predicate, object));
    }
    
    @Test
    public void Two_Patterns(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(
                   Blocks.pattern(subject, RDF.type,  RDFS.Class),
                   Blocks.pattern(subject, predicate, object));
    }
    
    @Test
    public void Group(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .construct(
                   Blocks.pattern(subject, RDF.type,  RDFS.Class),
                   Blocks.pattern(subject, predicate, object));
    }


}
