package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public class RDFXMLWriter implements RDFWriter{

    private final Writer writer;
    
    private final Map<String, String> prefixes;
    
    @Nullable
    private STMT last;
    
    public RDFXMLWriter(Writer writer, Map<String,String> prefixes) {
        this.writer = writer;
        this.prefixes = prefixes;
    }
    
    @Override
    public void begin() {
        try {
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<!DOCTYPE rdf:RDF [\n");
            for (Map.Entry<String,String> entry : prefixes.entrySet()){
                writer.append("<!ENTITY "+entry.getValue()+" \""+entry.getKey()+"\">\n");
            }
            writer.append("]>\n");
            
            writer.append("<rdf:RDF xmlns:rdf=\"&rdf;\"");
            for (Map.Entry<String,String> entry : prefixes.entrySet()){
                if (!entry.getKey().equals(RDF.NS)){
                    writer.append(" xmlns:"+entry.getValue()+"=\"&" + entry.getValue() + ";\"");    
                }                
            }
            writer.append(">\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void end() {        
        try {
            if (last != null){
                writer.append("  </rdf:Description>\n");
            }
            writer.append("</rdf:RDF>\n");
            writer.flush();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    @Override
    public void handle(STMT stmt) {
        try {
            if (last == null || !last.getSubject().equals(stmt.getSubject())){
                if (last != null){
                    writer.append("  </rdf:Description>\n");
                }
                writer.append("  <rdf:Description");
                if (stmt.getSubject().isURI()){
                    writer.append(" rdf:about=\"" + shorten(stmt.getSubject().asURI()) + "\">\n");    
                }else{
                    writer.append(" rdf:nodeID=\"" + stmt.getSubject().getId() + "\">\n");
                }
                
            }
            String prefix = prefixes.get(stmt.getPredicate().ns());
            if (prefix == null){
                prefix = "ns";
                writer.append("    <"+prefix+":"+stmt.getPredicate().ln() + " xmlns:ns=\"" + stmt.getPredicate().ns()+ "\"");    
            }else{
                writer.append("    <"+prefix+":"+stmt.getPredicate().ln());
            }            
            if (stmt.getObject().isLiteral()){
                LIT lit = stmt.getObject().asLiteral();
                if (lit.getLang() != null){
                    writer.append(" xml:lang=\"" + LocaleUtil.toLang(lit.getLang()) + "\"");
                }else{
                    writer.append(" rdf:datatype=\"" + shorten(lit.getDatatype()) + "\"");
                }
                writer.append(">");
                appendEscaped(stmt.getObject().getValue());
                writer.append("</"+prefix+":"+stmt.getPredicate().ln()+">\n");
            }else if (stmt.getObject().isBNode()){    
                writer.append(" rdf:nodeID=\"" + stmt.getObject().getValue() + "\"/>\n");
            }else{
                writer.append(" rdf:resource=\"" + shorten(stmt.getObject().asURI()) + "\"/>\n");
            }            
            last = stmt;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    
    private String shorten(UID uid) {
        if (prefixes.containsKey(uid.ns())){
            return "&" + prefixes.get(uid.ns()) + ";" + uid.ln();
        }else{
            return uid.getId();
        }
    }

    private void appendEscaped(String val) throws IOException {
        for (int i = 0; i < val.length(); ++i) {
            char c = val.charAt(i);
            if (c == '&') {
                writer.write("&amp;");
            } else if (c == '<') {
                writer.write("&lt;");
            } else if (c == '>') {
                writer.write("&gt;");
            } else if (c == '"') {
                writer.write("&quot;");
            } else if (c == '\'') {
                writer.write("&apos;");
            } else {
                writer.write(c);
            }
        }
    }

}
