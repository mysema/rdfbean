package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.MultimapFactory;

public class PropertiesMap {

    @Nullable
    private final Multimap<UID, STMT> direct;

    @Nullable
    private final Multimap<UID, STMT> inverse;

    public PropertiesMap() {
        this(MultimapFactory.<UID, STMT> create(), MultimapFactory.<UID, STMT> create());
    }

    public PropertiesMap(@Nullable Multimap<UID, STMT> direct, @Nullable Multimap<UID, STMT> inverse) {
        this.direct = direct;
        this.inverse = inverse;
    }

    @Nullable
    public Multimap<UID, STMT> getDirect() {
        return direct;
    }

    @Nullable
    public Multimap<UID, STMT> getInverse() {
        return inverse;
    }

}
