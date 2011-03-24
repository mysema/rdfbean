package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.SKOS;
import com.mysema.rdfbean.model.UID;

public class AbstractQueryImplTest {

    @Test
    public void Normalize(){
        String queryString = "SELECT * WHERE {\n" +
            "?dimensionType ?_c2 ?_c3 ; ?_c4 ?dimensionTypeName .\n" +
            "?dimension ?_c7 ?dimensionType ; ?_c4 ?dimensionName .\n" +
            "OPTIONAL {?dimension ?_c9 ?dimensionDescription }\n" +
            "OPTIONAL {?dimension ?_c11 ?parent }\n" +
            "}\n"+
            "ORDER BY ?dimensionName";

        Map<String, NODE> bindings = new HashMap<String, NODE>();
        bindings.put("_c9",  DC.description);
        bindings.put("_c2",  RDFS.subClassOf);
        bindings.put("_c11", SKOS.broader);
        bindings.put("_c7",  RDF.type);
        bindings.put("_c4",  DC.title);
        bindings.put("_c3",  new UID(TEST.NS, "Dimension"));

        List<NODE> nodes = new ArrayList<NODE>();
        String modifiedQuery = AbstractQueryImpl.normalize(queryString, bindings, nodes, true);
        System.err.println(modifiedQuery);
        assertEquals(bindings.get("_c2"),  nodes.get(0));
        assertEquals(bindings.get("_c3"),  nodes.get(1));
        assertEquals(bindings.get("_c4"),  nodes.get(2));
        assertEquals(bindings.get("_c7"),  nodes.get(3));
        assertEquals(bindings.get("_c4"),  nodes.get(4));
        assertEquals(bindings.get("_c9"),  nodes.get(5));
        assertEquals(bindings.get("_c11"), nodes.get(6));
    }

}
