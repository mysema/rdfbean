package com.mysema.rdfbean.model;

import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;

public class GraphQueryTest {
    
    private static final Path<ID> subject = new PathImpl<ID>(ID.class, "s");
    
    private static final Path<UID> predicate = new PathImpl<UID>(UID.class, "p");
    
    private static final Path<NODE> object = new PathImpl<NODE>(NODE.class, "o");
    
    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    private SPARQLVisitor visitor = new SPARQLVisitor();
    
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
        visitor.visit(metadata, QueryLanguage.GRAPH);
        System.out.println(visitor.toString());
    }

}
