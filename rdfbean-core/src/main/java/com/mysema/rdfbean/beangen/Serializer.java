/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.beangen;

import java.io.IOException;
import java.io.Writer;

/**
 * Serializer provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface Serializer {
    
    /**
     * @param model
     * @param writer
     * @throws IOException 
     */
    void serialize(BeanType model, Writer writer) throws IOException;
    
    /**
     * @param model
     * @param writer
     * @throws IOException 
     */
    void serialize(EnumType model, Writer writer) throws IOException;

}
