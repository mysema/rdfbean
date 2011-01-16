package com.mysema.rdfbean.sparql;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.SPARQLQuery;

public class JSONResultProducer extends AbstractResultProducer {

    @Override
    public void stream(SPARQLQuery query, Writer writer) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();  
        JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        if (query.getResultType().equals(SPARQLQuery.ResultType.BOOLEAN)){
            streamBoolean(query, generator);
        }else{
            streamTuple(query, generator);
        }            
        generator.flush();
    }
   
    private void streamBoolean(SPARQLQuery query, JsonGenerator generator) throws IOException {
        generator.writeStartObject(); 
        generator.writeNullField("head");
        generator.writeBooleanField("boolean", query.getBoolean());
        generator.writeEndObject();           
    }    

    private void streamTuple(SPARQLQuery query, JsonGenerator generator) throws JsonGenerationException, IOException {
        generator.writeStartObject();
        generator.writeObjectFieldStart("head");
        generator.writeArrayFieldStart("vars");
        for (String var : query.getVariables()){
            generator.writeString(var);
        }
        generator.writeEndArray();  // vars
        generator.writeEndObject(); // head
        
        generator.writeObjectFieldStart("results");
        generator.writeArrayFieldStart("bindings");
                
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            generator.writeStartObject();
            for (Map.Entry<String,NODE> entry : row.entrySet()){
                generator.writeObjectFieldStart(entry.getKey());
                generator.writeStringField("type", getNodeType(entry.getValue()));
                if (entry.getValue().isLiteral()){
                    LIT literal = entry.getValue().asLiteral();
                    if (literal.getLang() != null){
                        generator.writeStringField("xml:lang", LocaleUtil.toLang(literal.getLang()));
                    }else if (!literal.getDatatype().equals(RDF.text)){
                        generator.writeStringField("datatype", literal.getDatatype().getValue());
                    }
                }
                generator.writeStringField("value", entry.getValue().getValue());
                generator.writeEndObject();
            }
            generator.writeEndObject();
        }
        rows.close();
        
        generator.writeEndArray();  // bindings
        generator.writeEndObject(); // results
        generator.writeEndObject(); // root
    }

}
