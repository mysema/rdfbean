package com.mysema.rdfbean.sesame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.model.UID;
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
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        
        query();
    }    

    @Test
    public void Pattern_with_Filters(){
        Block pattern = Blocks.pattern(subject, predicate, object); 
        
        List<Predicate> filters = Arrays.<Predicate>asList(
                subject.eq(new UID(TEST.NS)),
                predicate.eq(RDFS.label),
                subject.ne(new UID(TEST.NS)),
                object.isNull(),
                object.isNotNull()
        );
        
        for (Predicate filter : filters){
            metadata = new DefaultQueryMetadata();
            metadata.addProjection(subject);
            metadata.addWhere(pattern, filter);
            query();
        }
    }
        
    @Test
    public void Pattern_with_Limit_and_Offset(){
        metadata.addProjection(subject);
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        metadata.setLimit(5l);
        metadata.setOffset(20l);
        
        query();
    }
    
    @Test
    public void Group(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        
        query();
    }
    
    @Test
    public void Union(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Blocks.union(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)
                ));
        
        query();
    }
    
    @Test
    public void Optional(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.optional(Blocks.pattern(subject, predicate, object)));
        
        query();
    }
    

    private void query() {
        TupleQuery query = session.createQuery(QueryLanguage.TUPLE, metadata);
        IteratorAdapter.asList(query.getTuples());
        
    }
}
