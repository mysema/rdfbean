package com.mysema.rdfbean.domains;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import org.joda.time.DateTime;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PDateTime;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PNumber;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PString;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface BeanSubQueryDomain {
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity {
       
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public long revision;
        
        @Predicate
        public DateTime created;
        
        @Predicate
        public String text;
        
        public Entity(){}
        
        public Entity(long rev, String t, DateTime c){
            revision = rev;
            text = t;
            created = c;
        }

        public long getRevision() {
            return revision;
        }

        public void setRevision(long revision) {
            this.revision = revision;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public DateTime getCreated() {
            return created;
        }

        public void setCreated(DateTime created) {
            this.created = created;
        }       
                                
    }
    
    public class QEntity extends PEntity<BeanSubQueryDomain.Entity> {

        private static final long serialVersionUID = 582395858;

        public static final QEntity entity = new QEntity("entity");

        public final PDateTime<org.joda.time.DateTime> created = createDateTime("created", org.joda.time.DateTime.class);

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PNumber<Long> revision = createNumber("revision", Long.class);

        public final PString text = createString("text");

        public QEntity(String variable) {
            super(BeanSubQueryDomain.Entity.class, forVariable(variable));
        }

        public QEntity(PEntity<? extends BeanSubQueryDomain.Entity> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity(PathMetadata<?> metadata) {
            super(BeanSubQueryDomain.Entity.class, metadata);
        }

    }

}
