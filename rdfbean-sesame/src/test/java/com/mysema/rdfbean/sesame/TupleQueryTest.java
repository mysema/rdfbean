package com.mysema.rdfbean.sesame;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.GroupBlock;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnionBlock;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({})
public class TupleQueryTest extends SessionTestBase{
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");

    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    @Test
    public void Pattern(){
        metadata.addProjection(subject);
        metadata.addWhere(PatternBlock.create(subject, RDF.type, RDFS.Class));
        
        query();
    }    

    @Test
    public void Pattern_with_Eq_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                GroupBlock.filter(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    subject.eq(new UID(TEST.NS))));
        
        query();
    }
    
    @Test
    public void Pattern_with_Ne_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                GroupBlock.filter(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    subject.ne(new UID(TEST.NS))));
        
        query();
    }
    
    @Test
    public void Pattern_with_NotNull_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                GroupBlock.filter(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    subject.isNotNull()));
        
        query();
    }
    
    @Test
    public void Pattern_with_Null_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                GroupBlock.filter(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    subject.isNull()));
        
        query();
    }
    
    @Test
    public void Pattern_with_Limit_and_Offset(){
        metadata.addProjection(subject);
        metadata.addWhere(PatternBlock.create(subject, RDF.type, RDFS.Class));
        metadata.setLimit(5l);
        metadata.setOffset(20l);
        
        query();
    }
    
    @Test
    public void Group(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                GroupBlock.create(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    PatternBlock.create(subject, predicate, object)
                ));
        
        query();
    }
    
    @Test
    public void Union(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                UnionBlock.create(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    PatternBlock.create(subject, predicate, object)
                ));
        
        query();
    }
    
    @Test
    public void Optional(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                GroupBlock.create(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    GroupBlock.optional(PatternBlock.create(subject, predicate, object))
                ));
        
        query();
    }
    

    private void query() {
        TupleQuery query = session.createQuery(QueryLanguage.TUPLE, metadata);
        IteratorAdapter.asList(query.getTuples());
        
    }
}
