package com.mysema.rdfbean.mulgara;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWL;

/**
 * MulgaraConnectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class MulgaraConnectionTest extends AbstractMulgaraTest{
    
    @Test    
    public void findStatements() throws IOException{
        assertEquals(2, count(connection.findStatements(null, null, OWL.Class, new UID("test:context1"), false)));
        assertEquals(2, count(connection.findStatements(null, null, OWL.Class, null, false)));
    }
}
