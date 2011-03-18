/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface NoteRevisionTermDomain {


    @ClassMapping
    public static class NoteRevision {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public String lemma;

        @Predicate
        public Note note;

        public String getLemma() {
            return lemma;
        }

        public Note getNote() {
            return note;
        }

    }

    @ClassMapping
    public static class Note {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public Term term;

        @Predicate
        public NoteRevision latestRevision;

        public Term getTerm() {
            return term;
        }

        public NoteRevision getLatestRevision() {
            return latestRevision;
        }

    }

    @ClassMapping
    public static class Term{

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public String basicForm;

        @Predicate
        public String meaning;

        public String getBasicForm(){
            return basicForm;
        }

        public String getMeaning() {
            return meaning;
        }
    }

    public class QNote extends EntityPathBase<NoteRevisionTermDomain.Note> {

        private static final long serialVersionUID = 1496561659;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QNote note = new QNote("note");

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final QNoteRevision latestRevision;

        public final QTerm term;

        public QNote(String variable) {
            this(NoteRevisionTermDomain.Note.class, forVariable(variable), INITS);
        }

        public QNote(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QNote(PathMetadata<?> metadata, PathInits inits) {
            this(NoteRevisionTermDomain.Note.class, metadata, inits);
        }

        public QNote(Class<? extends NoteRevisionTermDomain.Note> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.latestRevision = inits.isInitialized("latestRevision") ? new QNoteRevision(forProperty("latestRevision"), inits.get("latestRevision")) : null;
            this.term = inits.isInitialized("term") ? new QTerm(forProperty("term")) : null;
        }

    }

    public class QNoteRevision extends EntityPathBase<NoteRevisionTermDomain.NoteRevision> {

        private static final long serialVersionUID = -1259034378;

        private static final PathInits INITS = PathInits.DIRECT;

        public static final QNoteRevision noteRevision = new QNoteRevision("noteRevision");

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final StringPath lemma = createString("lemma");

        public final QNote note;

        public QNoteRevision(String variable) {
            this(NoteRevisionTermDomain.NoteRevision.class, forVariable(variable), INITS);
        }

        public QNoteRevision(PathMetadata<?> metadata) {
            this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
        }

        public QNoteRevision(PathMetadata<?> metadata, PathInits inits) {
            this(NoteRevisionTermDomain.NoteRevision.class, metadata, inits);
        }

        public QNoteRevision(Class<? extends NoteRevisionTermDomain.NoteRevision> type, PathMetadata<?> metadata, PathInits inits) {
            super(type, metadata, inits);
            this.note = inits.isInitialized("note") ? new QNote(forProperty("note"), inits.get("note")) : null;
        }

    }

    public class QTerm extends EntityPathBase<NoteRevisionTermDomain.Term> {

        private static final long serialVersionUID = 1496730741;

        public static final QTerm term = new QTerm("term");

        public final StringPath basicForm = createString("basicForm");

        public final SimplePath<com.mysema.rdfbean.model.ID> id = createSimple("id", com.mysema.rdfbean.model.ID.class);

        public final StringPath meaning = createString("meaning");

        public QTerm(String variable) {
            super(NoteRevisionTermDomain.Term.class, forVariable(variable));
        }

        public QTerm(EntityPathBase<? extends NoteRevisionTermDomain.Term> entity) {
            super(entity.getType(),entity.getMetadata());
        }

        public QTerm(PathMetadata<?> metadata) {
            super(NoteRevisionTermDomain.Term.class, metadata);
        }

    }

}
