/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import org.joda.time.DateTime;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

import static com.mysema.query.types.PathMetadataFactory.*;

public interface EntityDomain {
    
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
        
        @Predicate
        public String property;
        
        @Predicate
        public String text1;
        
        @Predicate
        public String text2;
        
        public String getProperty(){
            return property;
        }
        
        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }
        
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
    
    public class QEntity extends EntityPathBase<EntityDomain.Entity> {

        private static final long serialVersionUID = 582395858;

        public static final QEntity entity = new QEntity("entity");

        public final DateTimePath<org.joda.time.DateTime> created = createDateTime("created", org.joda.time.DateTime.class);

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final NumberPath<Long> revision = createNumber("revision", Long.class);

        public final StringPath text = createString("text");
        
        public final StringPath property = createString("property");
        
        public final StringPath text1 = createString("text1");

        public final StringPath text2 = createString("text2");

        public QEntity(String variable) {
            super(EntityDomain.Entity.class, forVariable(variable));
        }

        public QEntity(EntityPathBase<? extends EntityDomain.Entity> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QEntity(PathMetadata<?> metadata) {
            super(EntityDomain.Entity.class, metadata);
        }

    }

}
