/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import java.util.HashSet;
import java.util.Set;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.SetPath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

import static com.mysema.query.types.PathMetadataFactory.*;

public interface UserProfileDomain {
    
    @ClassMapping(ns=TEST.NS)
    public abstract class Identifiable {
        
        private static final long serialVersionUID = 4580448045434144592L;
        
        @Id(IDType.LOCAL)
        public String id;
        
        public String getId() {
            return id;
        }

        public String toString() {
            return id;
        }

    }
    
    @ClassMapping(ns=TEST.NS)
    public class User extends Identifiable {
        
        @Predicate
        public String firstName, lastName, email;
        
        @Predicate
        public String username;
        
        @Predicate
        public String password;

        @Predicate
        public Profile profile;
        
        @Predicate
        private Set<User> buddies = new HashSet<User>();
        
        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public Profile getProfile() {
            return profile;
        }

        public Set<User> getBuddies() {
            return buddies;
        }
        
        public boolean isActive(){
            return true;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public enum Profile {
        User,
        Admin        
    }
    
    public class QIdentifiable extends EntityPathBase<UserProfileDomain.Identifiable> {

        private static final long serialVersionUID = -1841937934;

        public static final QIdentifiable identifiable = new QIdentifiable("identifiable");

        public final StringPath id = createString("id");

        public QIdentifiable(String variable) {
            super(UserProfileDomain.Identifiable.class, forVariable(variable));
        }

        public QIdentifiable(EntityPathBase<? extends UserProfileDomain.Identifiable> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QIdentifiable(PathMetadata<?> metadata) {
            super(UserProfileDomain.Identifiable.class, metadata);
        }

    }
    
    public class QProfile extends EntityPathBase<UserProfileDomain.Profile> {

        private static final long serialVersionUID = 1043504269;

        public static final QProfile profile = new QProfile("profile");

        public QProfile(String variable) {
            super(UserProfileDomain.Profile.class, forVariable(variable));
        }

        public QProfile(EntityPathBase<? extends UserProfileDomain.Profile> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QProfile(PathMetadata<?> metadata) {
            super(UserProfileDomain.Profile.class, metadata);
        }

    }
    
    public class QUser extends EntityPathBase<UserProfileDomain.User> {

        private static final long serialVersionUID = -1810734233;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QUser user = new QUser("user");

        public final QIdentifiable _super = new QIdentifiable(this);

        public final SetPath<UserProfileDomain.User> buddies = createSet("buddies", UserProfileDomain.User.class);

        public final StringPath email = createString("email");

        public final StringPath firstName = createString("firstName");

        //inherited
        public final StringPath id = _super.id;

        public final StringPath lastName = createString("lastName");

        public final StringPath password = createString("password");

        public final QProfile profile;

        public final StringPath username = createString("username");

        public QUser(String variable) {
            this(UserProfileDomain.User.class, forVariable(variable), INITS);
        }

        public QUser(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QUser(PathMetadata<?> metadata, PathInits inits) {
            this(UserProfileDomain.User.class, metadata, inits);
        }

        public QUser(Class<? extends UserProfileDomain.User> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.profile = inits.isInitialized("profile") ? new QProfile(forProperty("profile")) : null;
        }

    }

}
