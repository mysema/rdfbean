package com.mysema.rdfbean.sparql;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.mysema.commons.fluxml.XMLWriter;
import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.SPARQLQuery;

/**
 * @author tiwe
 *
 */
public class XMLResultProducer extends AbstractResultProducer{

    private static final String SPARQL_NS = "http://www.w3.org/2005/sparql-results#";
    
    @Override
    public void stream(SPARQLQuery query, Writer w) throws IOException {
        XMLWriter writer = new XMLWriter(w);
        if (query.getResultType().equals(SPARQLQuery.ResultType.BOOLEAN)){
            streamBoolean(query, writer);
        }else{
            streamTuple(query, writer);
        }                 
    }

    private void streamBoolean(SPARQLQuery query, XMLWriter writer) throws IOException {
        writer.begin("sparql");
        writer.attribute("xmlns", SPARQL_NS);
        writer.element("head");
        writer.begin("results");
        writer.element("boolean", query.getBoolean());
        writer.end("results");
        writer.end("sparql");
    }
    private void streamTuple(SPARQLQuery query, XMLWriter writer) throws IOException {
        writer.begin("sparql");
        writer.attribute("xmlns", SPARQL_NS);
        writer.begin("head");
        for (String var : query.getVariables()) {
            writer.begin("variable").attribute("name", var).end("variable");
        }
        writer.end("head");
        writer.begin("results");
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            writer.begin("result");
            for (Map.Entry<String, NODE> entry : row.entrySet()) {
                writer.begin("binding").attribute("name", entry.getKey());
                String type = getNodeType(entry.getValue());
                writer.begin(type);
                if (entry.getValue().isLiteral()){
                    LIT literal = entry.getValue().asLiteral();
                    if (literal.getLang() != null){
                        writer.attribute("xml:lang", LocaleUtil.toLang(literal.getLang()));
                    }else if (!literal.getDatatype().equals(RDF.text)){
                        writer.attribute("datatype", literal.getDatatype().getValue());
                    }
                }
                writer.print(entry.getValue().getValue());
                writer.end(type);
                writer.end("binding");
            }
            writer.end("result");
        }
        writer.end("results");
        writer.end("sparql");
    }


    
}
