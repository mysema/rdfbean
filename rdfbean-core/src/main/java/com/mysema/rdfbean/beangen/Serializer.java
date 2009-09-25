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
    void serialize(BeanModel model, Writer writer) throws IOException;
    
    /**
     * @param model
     * @param writer
     * @throws IOException 
     */
    void serialize(EnumModel model, Writer writer) throws IOException;

    /**
     * @param usePrimitives
     */
    void setUsePrimitives(boolean usePrimitives);

}
