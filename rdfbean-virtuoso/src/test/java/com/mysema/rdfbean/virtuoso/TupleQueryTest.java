package com.mysema.rdfbean.virtuoso;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;
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
import com.mysema.rdfbean.owl.OWL;


public class TupleQueryTest extends AbstractConnectionTest {
    
    private static final Path<ID> subject = new PathImpl<ID>(ID.class, "s");
    
    private static final Path<UID> predicate = new PathImpl<UID>(UID.class, "p");
    
    private static final Path<NODE> object = new PathImpl<NODE>(NODE.class, "o");

    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    @Test
    public void Pattern(){
        metadata.addProjection(subject);
        metadata.addWhere(PatternBlock.create(subject, RDF.type, RDFS.Class));
        
        query();
    }
    
    @Test
    public void Pattern_with_Group(){
        metadata.addProjection(subject);
        metadata.addWhere(PatternBlock.create(subject, RDF.type, RDFS.Class));
        metadata.addGroupBy(subject);
        
        query();
    }
    

    @Test
    @Ignore
    public void Pattern_with_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                GroupBlock.filter(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    ExpressionUtils.isNotNull(subject)));
        
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
        metadata.addProjection(subject);
        metadata.addWhere(
                UnionBlock.create(
                    PatternBlock.create(subject, RDF.type, RDFS.Class),
                    PatternBlock.create(subject, RDF.type, OWL.Class)
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
        TupleQuery query = connection.createQuery(QueryLanguage.TUPLE, metadata);
        IteratorAdapter.asList(query.getTuples());
        
    }
}
