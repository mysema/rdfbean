package com.mysema.rdfbean.domains;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PString;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface PagingDomain {
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity{
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public String property;
        
        public String getProperty(){
            return property;
        }
    }
    
    public class QEntity extends PEntity<PagingDomain.Entity> {

        private static final long serialVersionUID = -1794046786;

        public static final QEntity entity = new QEntity("entity");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PString property = createString("property");

        public QEntity(String variable) {
            super(PagingDomain.Entity.class, forVariable(variable));
        }

        public QEntity(PEntity<? extends PagingDomain.Entity> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity(PathMetadata<?> metadata) {
            super(PagingDomain.Entity.class, metadata);
        }

    }
    

}
