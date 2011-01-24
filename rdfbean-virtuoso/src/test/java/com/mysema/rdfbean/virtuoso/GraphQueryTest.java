package com.mysema.rdfbean.virtuoso;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;
import com.mysema.rdfbean.model.GraphQuery;
import com.mysema.rdfbean.model.GroupBlock;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

public class GraphQueryTest extends AbstractConnectionTest{
    
    private static final Path<ID> subject = new PathImpl<ID>(ID.class, "s");
    
    private static final Path<UID> predicate = new PathImpl<UID>(UID.class, "p");
    
    private static final Path<NODE> object = new PathImpl<NODE>(NODE.class, "o");
    
    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    @Test
    public void Pattern(){
        metadata.addProjection(PatternBlock.create(subject, predicate, object));
        metadata.addWhere(
                GroupBlock.create(
                        PatternBlock.create(subject, RDF.type, RDFS.Class),
                        PatternBlock.create(subject, predicate, object)));
        
        query();
    }
    
    @Test
    public void Two_Patterns(){
        metadata.addProjection(
                PatternBlock.create(subject, RDF.type,  RDFS.Class),
                PatternBlock.create(subject, predicate, object));
        metadata.addWhere(
                GroupBlock.create(
                        PatternBlock.create(subject, RDF.type, RDFS.Class),
                        PatternBlock.create(subject, predicate, object)));
        
        query();
    }
    
    @Test
    public void Group(){
        metadata.addProjection(
                GroupBlock.create(
                        PatternBlock.create(subject, RDF.type,  RDFS.Class),
                        PatternBlock.create(subject, predicate, object)));
        metadata.addWhere(
                GroupBlock.create(
                        PatternBlock.create(subject, RDF.type, RDFS.Class),
                        PatternBlock.create(subject, predicate, object)));
        
        query();
    }
    
    private void query() {
        GraphQuery query = connection.createQuery(QueryLanguage.GRAPH, metadata);
        IteratorAdapter.asList(query.getTriples());
        
    }

}
