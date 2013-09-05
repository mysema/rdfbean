/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import org.joda.time.DateTime;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.RDFS;

public interface SongPlaybackMusicStore {

    @ClassMapping(ns = "http://www.foo.com#")
    public class Song {

        @Id(IDType.RESOURCE)
        public ID id;

        public ID getId() {
            return id;
        }

        @Predicate(ns = RDFS.NS)
        public String label;

    }

    @ClassMapping(ns = "http://www.foo.com#")
    public class SongPlayback {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate(inv = true, ln = "playback")
        public ID song;

        @Predicate(ln = "musicStore")
        public ID store;
        
        @Predicate
        public DateTime datetime;

    }

    @ClassMapping(ns = "http://www.foo.com#")
    public class MusicStore {

        @Id(IDType.RESOURCE)
        public ID id;

        public ID getId() {
            return id;
        }

        @Predicate(ns = RDFS.NS)
        public String label;
    }

}
