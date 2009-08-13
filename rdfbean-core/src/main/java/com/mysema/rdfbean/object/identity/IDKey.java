/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.Serializable;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;

/**
 * IDKey defines a single model and id pair for ID/LID mapping
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Immutable
public final class IDKey implements Serializable {
    
    private static final long serialVersionUID = 2052361909789383871L;

    @Nullable
    private final ID model;
    
    private final ID id;

    public IDKey(ID uid) {
        this(null, uid);
    }

    public IDKey(ID model, BID bid) {
        this(Assert.notNull(model), (ID) bid);
    }

    public IDKey(@Nullable ID model, ID id) {
        this.model = model;
        this.id = Assert.notNull(id);
    }

    public ID getModel() {
        return model;
    }

    public ID getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return 31 * id.hashCode() + ((model == null) ? 0 : model.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof IDKey)) {
            return false;
        }
        IDKey other = (IDKey) obj;
        if (model == null) {
            if (other.model != null) {
                return false;
            }
        } else if (!model.equals(other.model)) {
            return false;
        }
        return id.equals(other.id);
    }
}