/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * @author tiwe
 *
 */
public interface Operation<RT> {

    /**
     * @return
     * @throws IOException 
     */
    @Nullable
    RT execute(RDFConnection connection) throws IOException;
    
}
