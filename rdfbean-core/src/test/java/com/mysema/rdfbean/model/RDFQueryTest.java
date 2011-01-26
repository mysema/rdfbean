package com.mysema.rdfbean.model;

import java.util.Map;

import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.EmptyCloseableIterator;
import com.mysema.query.types.Expression;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.TEST;

public class RDFQueryTest {
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");
    
    private RDFConnection conn = new MiniRepository().openConnection();

    private RDFQuery query(){
        return new RDFQueryImpl(conn){
            @Override
            public CloseableIterator<Map<String,NODE>> select(Expression<?>... exprs){
                queryMixin.addToProjection(exprs);
                SPARQLVisitor visitor = new SPARQLVisitor();
                visitor.visit(queryMixin.getMetadata(), QueryLanguage.TUPLE);
                System.out.println(visitor.toString());
                return new EmptyCloseableIterator<Map<String,NODE>>();
            }
        };
    }
    
    @Test
    public void Pattern(){
        query().where(Blocks.pattern(subject, RDF.type, RDFS.Class)).select(subject);        
    }
    
    @Test
    public void Pattern_with_Eq_Filter(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.eq(new UID(TEST.NS))).select(subject);        
    }
    
    @Test
    public void Pattern_with_Ne_Filter(){
        query().where(
                Blocks.pattern(subject, RDF.type, object),
                subject.ne(new UID(TEST.NS))).select(subject);
    }
    
    @Test
    public void Pattern_with_NotNull_Filter(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.isNotNull()).select(subject);
    }
    
    @Test
    public void Pattern_with_Null_Filter(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                subject.isNull()).select(subject);
    }
    
    @Test
    public void Pattern_with_Limit_and_Offset(){
        query().where(Blocks.pattern(subject, RDF.type, RDFS.Class))
                .limit(5)
                .offset(5)
                .select(subject);
    }
    
    @Test
    public void Group(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.pattern(subject, predicate, object))
               .select(subject, predicate, object);
    }
    
    @Test
    public void Union(){
        query().where(
                Blocks.union(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)
                )).select(subject, predicate, object);
    }
    
    @Test
    public void Optional(){
        query().where(
                Blocks.pattern(subject, RDF.type, RDFS.Class),
                Blocks.optional(Blocks.pattern(subject, predicate, object)
            )).select(subject, predicate, object);
    }
    
}
