package com.mysema.rdfbean.virtuoso;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWL;


public class TupleQueryTest extends AbstractConnectionTest {
    
    private static final SimplePath<ID> subject = new SimplePath<ID>(ID.class, "s");
    
    private static final SimplePath<UID> predicate = new SimplePath<UID>(UID.class, "p");
    
    private static final SimplePath<NODE> object = new SimplePath<NODE>(NODE.class, "o");

    private QueryMetadata metadata = new DefaultQueryMetadata();
    
    @Test
    public void Pattern(){
        metadata.addProjection(subject);
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        metadata.setLimit(1l);
        
        query();
    }
    
    @Test
    public void Pattern_with_Group(){
        metadata.addProjection(subject);
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        metadata.addGroupBy(subject);
        metadata.setLimit(1l);
        
        query();
    }
    

    @Test
    @Ignore
    public void Pattern_with_Filter(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.filter(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    ExpressionUtils.isNotNull(subject)));
        metadata.setLimit(1l);
        
        query();
    }
    
    @Test
    public void Pattern_with_Limit_and_Offset(){
        metadata.addProjection(subject);
        metadata.addWhere(Blocks.pattern(subject, RDF.type, RDFS.Class));
        metadata.setLimit(5l);
        metadata.setOffset(20l);
        metadata.setLimit(1l);
        
        query();
    }
    
    @Test
    public void Group(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, predicate, object)
                ));
        metadata.setLimit(1l);
        
        query();
    }
    
    @Test
    public void Union(){
        metadata.addProjection(subject);
        metadata.addWhere(
                Blocks.union(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.pattern(subject, RDF.type, OWL.Class)
                ));
        metadata.setLimit(1l);
        
        query();
    }
    
    @Test
    public void Optional(){
        metadata.addProjection(subject, predicate, object);
        metadata.addWhere(
                Blocks.group(
                    Blocks.pattern(subject, RDF.type, RDFS.Class),
                    Blocks.optional(Blocks.pattern(subject, predicate, object))
                ));
        metadata.setLimit(1l);
        
        query();
    }
    

    private void query() {
        TupleQuery query = connection.createQuery(QueryLanguage.TUPLE, metadata);
        IteratorAdapter.asList(query.getTuples());
        
    }
}
