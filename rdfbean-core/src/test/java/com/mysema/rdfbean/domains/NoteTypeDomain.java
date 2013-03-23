package com.mysema.rdfbean.domains;

import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface NoteTypeDomain {

    @ClassMapping
    public static class Note {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public NoteType type;

        @Predicate
        public Set<NoteType> types;

        public Note() {
        }

        public Note(NoteType type) {
            this.type = type;
        }

        public ID getId() {
            return id;
        }

        public NoteType getType() {
            return type;
        }

        public Set<NoteType> getTypes() {
            return types;
        }

    }

    @ClassMapping
    public enum NoteType {
        TYPE1,
        TYPE2,
        A,
        B
    }

}
