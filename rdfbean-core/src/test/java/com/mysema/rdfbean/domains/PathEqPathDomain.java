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

public interface PathEqPathDomain {
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity{
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public String text1;
        
        @Predicate
        public String text2;

        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }
        
    }
    
    public class QEntity extends PEntity<PathEqPathDomain.Entity> {

        private static final long serialVersionUID = -1983270252;

        public static final QEntity entity = new QEntity("entity");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PString text1 = createString("text1");

        public final PString text2 = createString("text2");

        public QEntity(String variable) {
            super(PathEqPathDomain.Entity.class, forVariable(variable));
        }

        public QEntity(PEntity<? extends PathEqPathDomain.Entity> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity(PathMetadata<?> metadata) {
            super(PathEqPathDomain.Entity.class, metadata);
        }

    }

}
