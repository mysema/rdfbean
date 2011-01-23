package com.mysema.rdfbean.model;

import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;

public class SPARQLVisitorTest {
    
    private static final Path<ID> subject = new PathImpl<ID>(ID.class, "s");
    
    private static final Path<UID> predicate = new PathImpl<UID>(UID.class, "p");
    
    private static final Path<NODE> object = new PathImpl<NODE>(NODE.class, "o");
    
    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    private SPARQLVisitor visitor = new SPARQLVisitor();

    @Test
    public void Pattern(){
        metadata.addProjection(subject);
        metadata.addWhere(Pattern.create(subject, RDF.type, RDFS.Class));
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
    @Test
    public void Pattern_with_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Group.filter(
                    Pattern.create(subject, RDF.type, RDFS.Class),
                    ExpressionUtils.isNotNull(subject)));
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
    @Test
    public void Pattern_with_Limit_and_Offset(){
        metadata.addProjection(subject);
        metadata.addWhere(Pattern.create(subject, RDF.type, RDFS.Class));
        metadata.setLimit(5l);
        metadata.setOffset(20l);
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
    @Test
    public void Group(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Group.create(
                    Pattern.create(subject, RDF.type, RDFS.Class),
                    Pattern.create(subject, predicate, object)
                ));
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
    @Test
    public void Union(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Union.create(
                    Pattern.create(subject, RDF.type, RDFS.Class),
                    Pattern.create(subject, predicate, object)
                ));
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
    @Test
    public void Optional(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Group.create(
                    Pattern.create(subject, RDF.type, RDFS.Class),
                    Group.optional(Pattern.create(subject, predicate, object))
                ));
        
        visitor.visit(metadata, null);
        System.out.println(visitor.toString());
    }
    
}
