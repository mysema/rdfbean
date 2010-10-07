package com.mysema.rdfbean.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mysema.commons.fluxml.XMLWriter;
import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.SPARQLQuery;

/**
 * @author tiwe
 *
 */
public class SPARQLResultProducer {

    private static final String SPARQL_NS = "http://www.w3.org/2005/sparql-results#";

    public void streamXMLResults(SPARQLQuery query, XMLWriter writer)
            throws IOException {
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
                String type;
                switch (entry.getValue().getNodeType()) {
                    case BLANK:   type = "bnode"; break;
                    case URI:     type = "uri"; break;
                    case LITERAL: type = "literal"; break;
                    default:      type = "null";
                }
                writer.begin(type);
                if (entry.getValue().isLiteral()){
                    LIT literal = entry.getValue().asLiteral();
                    if (literal.getLang() != null){
                        writer.attribute("xml:lang", LocaleUtil.toLang(literal.getLang()));
                    }else if (literal.getDatatype() != null){
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
    
    public void streamJSONResults(SPARQLQuery query, PrintWriter writer) {
        JSONObject root = new JSONObject();
        
        // head
        JSONObject head = new JSONObject();
        head.put("vars", JSONArray.fromObject(query.getVariables()));
        
        // results
        JSONObject results = new JSONObject();
        JSONArray bindings = new JSONArray();
                
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            JSONObject binding = new JSONObject();
            for (Map.Entry<String,NODE> entry : row.entrySet()){
                JSONObject value = new JSONObject();
                switch (entry.getValue().getNodeType()){
                    case URI: value.put("type", "uri"); break;
                    case BLANK: value.put("type", "bnode"); break;
                    case LITERAL: value.put("type", "literal"); break;
                }
                if (entry.getValue().isLiteral()){
                    LIT literal = entry.getValue().asLiteral();
                    if (literal.getLang() != null){
                        value.put("xml:lang", LocaleUtil.toLang(literal.getLang()));
                    }else if (literal.getDatatype() != null){
                        value.put("datatype", literal.getDatatype().getValue());
                    }
                }
                value.put("value", entry.getValue().getValue());
                binding.put(entry.getKey(), value);
            }
            bindings.add(binding);                              
        }
        
        results.put("bindings", bindings);        
        root.put("head", head);
        root.put("results", results);
        writer.print(root.toString());
    }

}
