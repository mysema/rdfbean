package com.mysema.rdfbean.model;

import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.SimplePath;

public class GraphQueryTest {
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");
    
    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    private SPARQLVisitor visitor = new SPARQLVisitor();
    
    @Test
    public void Patterns(){
        metadata.addProjection(Blocks.pattern(subject, predicate, object));
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        
        query();
    }
    
    @Test
    public void Patterns_as_Group(){
        metadata.addProjection(Blocks.pattern(subject, predicate, object));
        metadata.addWhere(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)));
        
        query();
    }
    
    @Test
    public void Two_Patterns(){
        metadata.addProjection(
                Blocks.pattern(subject, RDF.type,  RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        
        query();
    }
    
    @Test
    public void Group(){
        metadata.addProjection(
                Blocks.pattern(subject, RDF.type,  RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object));
        
        query();
    }
    
    private void query() {
        visitor.visit(metadata, QueryLanguage.GRAPH);
        System.out.println(visitor.toString());
    }

}
