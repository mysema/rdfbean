package com.mysema.rdfbean.sesame;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.GraphQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
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
        GraphQuery query = session.createQuery(QueryLanguage.GRAPH, metadata);
        IteratorAdapter.asList(query.getTriples());
        
    }

}
