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
public interface RDFConnectionCallback<RT> {

    /**
     * @return
     * @throws IOException
     */
    @Nullable
    RT doInConnection(RDFConnection connection) throws IOException;

}
