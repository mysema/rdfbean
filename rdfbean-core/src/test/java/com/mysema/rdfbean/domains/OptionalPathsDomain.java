package com.mysema.rdfbean.domains;

import static com.mysema.query.types.path.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PString;
import com.mysema.query.types.path.PathInits;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface OptionalPathsDomain {
    
    @ClassMapping(ns=TEST.NS)
    public static class Note {
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public String basicForm;        

        @Predicate
        public String lemma;        
        
        @Predicate
        public Term term;
        
        public String getBasicForm() {
            return basicForm;
        }
        
        public String getLemma() {
            return lemma;
        }
        
        public Term getTerm() {
            return term;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Term{

        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public String meaning;

        public String getMeaning() {
            return meaning;
        }                   
    }

    public class QNote extends PEntity<OptionalPathsDomain.Note> {

        private static final long serialVersionUID = 1879011921;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QNote note = new QNote("note");

        public final PString basicForm = createString("basicForm");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PString lemma = createString("lemma");

        public final QTerm term;

        public QNote(String variable) {
            this(OptionalPathsDomain.Note.class, forVariable(variable), INITS);
        }

        public QNote(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QNote(PathMetadata<?> metadata, PathInits inits) {
            this(OptionalPathsDomain.Note.class, metadata, inits);
        }

        public QNote(Class<? extends OptionalPathsDomain.Note> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.term = inits.isInitialized("term") ? new QTerm(forProperty("term")) : null;
        }

    }
    
    public class QTerm extends PEntity<OptionalPathsDomain.Term> {

        private static final long serialVersionUID = 1879181003;

        public static final QTerm term = new QTerm("term");

        public final PSimple<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final PString meaning = createString("meaning");

        public QTerm(String variable) {
            super(OptionalPathsDomain.Term.class, forVariable(variable));
        }

        public QTerm(PEntity<? extends OptionalPathsDomain.Term> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QTerm(PathMetadata<?> metadata) {
            super(OptionalPathsDomain.Term.class, metadata);
        }

    }
}
