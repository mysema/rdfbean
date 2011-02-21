package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class PropertiesMap {

    @Nullable
    private final MultiMap<UID, STMT> direct;

    @Nullable
    private final MultiMap<UID, STMT> inverse;

    public PropertiesMap(){
        this(new MultiHashMap<UID, STMT>(), new MultiHashMap<UID, STMT>());
    }

    public PropertiesMap(@Nullable MultiMap<UID, STMT> direct, @Nullable MultiMap<UID, STMT> inverse) {
        this.direct = direct;
        this.inverse = inverse;
    }

    @Nullable
    public MultiMap<UID, STMT> getDirect() {
        return direct;
    }

    @Nullable
    public MultiMap<UID, STMT> getInverse() {
        return inverse;
    }

}
