package com.mysema.rdfbean.mulgara;

import java.net.URI;

import org.mulgara.query.Variable;
import org.mulgara.query.rdf.Mulgara;

/**
 * Constants provides
 *
 * @author tiwe
 * @version $Id$
 */
interface Constants {

    URI EMPTY_GRAPH = URI.create(Mulgara.NULL_GRAPH);
    
    Variable S_VAR = new Variable("s");
    
    Variable P_VAR = new Variable("p");
    
    Variable O_VAR = new Variable("o");
    
    Variable C_VAR = new Variable("c");
}
