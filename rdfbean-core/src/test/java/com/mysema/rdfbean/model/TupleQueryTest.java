package com.mysema.rdfbean.model;

import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.TEST;

public class TupleQueryTest {
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");
    
    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    private SPARQLVisitor visitor = new SPARQLVisitor();

    @Test
    public void Pattern(){
        metadata.addProjection(subject);
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        
        query();
    }
    
    @Test
    public void Pattern_with_Eq_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.eq(new UID(TEST.NS)));
        
        query();
    }
    
    @Test
    public void Pattern_with_Ne_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.ne(new UID(TEST.NS)));
        
        query();
    }
    
    @Test
    public void Pattern_with_NotNull_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.isNotNull());
        
        query();
    }
    
    @Test
    public void Pattern_with_Null_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.isNull());
        
        query();
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
        visitor.visit(metadata, QueryLanguage.TUPLE);
        System.out.println(visitor.toString());
    }
}
