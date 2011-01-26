package com.mysema.rdfbean.sesame;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.model.GraphQuery;
import com.mysema.rdfbean.model.GroupBlock;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({})
public class GraphQueryTest extends SessionTestBase{
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");
    
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
        GraphQuery query = session.createQuery(QueryLanguage.GRAPH, metadata);
        IteratorAdapter.asList(query.getTriples());
        
    }

}
