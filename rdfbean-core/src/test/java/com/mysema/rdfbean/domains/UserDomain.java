/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface UserDomain {


    @ClassMapping
    public static class User{

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public  String firstName;

        public User(){}

        public User(String firstName){
            this.firstName = firstName;
        }

        public String getFirstName(){
            return firstName;
        }

    }

    public class QUser extends EntityPathBase<UserDomain.User> {

        private static final long serialVersionUID = 790171466;

        public static final QUser user = new QUser("user");

        public final StringPath firstName = createString("firstName");

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public QUser(String variable) {
            super(UserDomain.User.class, forVariable(variable));
        }

        public QUser(EntityPathBase<? extends UserDomain.User> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QUser(PathMetadata<?> metadata) {
            super(UserDomain.User.class, metadata);
        }

    }


}
