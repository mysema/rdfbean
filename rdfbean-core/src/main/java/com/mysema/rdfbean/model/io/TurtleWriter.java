package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public class TurtleWriter implements RDFWriter{
    
    protected final Appendable appendable;
    
    private final boolean blankNodeAsURI;
    
    private final Map<String, String> prefixes;
    
    @Nullable
    protected STMT last;
        
    public TurtleWriter(Appendable writer, Map<String,String> prefixes) {
        this(writer, prefixes, false);
    }
    
    public TurtleWriter(Appendable writer, Map<String,String> prefixes, boolean blankNodeAsURI) {
        this.appendable = writer;
        this.prefixes = prefixes;
        this.blankNodeAsURI = blankNodeAsURI;
    }
    
    protected void append(BID bid) throws IOException{
        if (blankNodeAsURI){
            appendable.append("<_:").append(bid.getValue()).append(">");
        }else{
            appendable.append("_:").append(bid.getValue());    
        }        
    }
    
    protected void append(LIT lit) throws IOException{
        String val = lit.getValue();
        if (val.indexOf('\n') > 0 || val.indexOf('\r') > 0 || val.indexOf('\t') > 0) {
            appendable.append("\"\"\"");
            appendable.append(TurtleUtil.encodeLongString(val));
            appendable.append("\"\"\"");
        } else {
            appendable.append("\"");
            appendable.append(TurtleUtil.encodeString(val));
            appendable.append("\"");    
        }        
        
        if (lit.getLang() != null) {
            appendable.append("@").append(LocaleUtil.toLang(lit.getLang()));            
        } else if (!lit.getDatatype().equals(RDF.text)) {
            appendable.append("^^");
            append(lit.getDatatype());
        }
    }
    
    private void append(NODE node) throws IOException {
        if (node.isURI()) {
            append(node.asURI());
        } else if (node.isLiteral()) {
            append(node.asLiteral());
        } else {
            append(node.asBNode());
        }
    }
    
    protected void append(UID uid) throws IOException{
        if (uid.ln().length() == 0 || !TurtleUtil.isName(uid.ln())){
            appendFull(uid);
        }else{
            appendPrefixed(uid);
        }            
    }
    
    protected void appendPredicate(UID uid) throws IOException{
        if (uid.equals(RDF.type)){
            appendable.append("a");
        }else{
            append(uid);
        }
    }
    
    protected void appendFull(UID uid) throws IOException {
        appendable.append("<").append(uid.getValue()).append(">");
    }
    
    protected void appendPrefixed(UID uid) throws IOException {
        String prefix = prefixes.get(uid.ns());        
        if (prefix != null){
            appendable.append(prefix).append(":").append(uid.ln());    
        }else{
            appendable.append("<").append(NTriplesUtil.escapeString(uid.getId())).append(">");
        }
    }

    @Override
    public void begin(){
        try{
            for (Map.Entry<String,String> entry : prefixes.entrySet()){
                appendable.append("@prefix ");
                appendable.append(entry.getValue());
                appendable.append(": <");
                appendable.append(TurtleUtil.encodeString(entry.getKey()));
                appendable.append("> .\n");
            }
            appendable.append("\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }    
    }

    @Override
    public void end() {
        if (last != null){
            try {
                appendable.append(" .\n");
                if (appendable instanceof Writer){
                    ((Writer)appendable).flush();    
                }                
            } catch (IOException e) {
                throw new RepositoryException(e);
            }        
        }
    }
        
    @Override
    public void handle(STMT stmt){
        try{
            if (last == null || !last.getSubject().equals(stmt.getSubject())) {
                if (last != null){
                    appendable.append(" .\n");    
                }            
                append(stmt.getSubject());
                appendable.append(" ");
                appendPredicate(stmt.getPredicate());
                appendable.append(" ");                 

            } else if (!last.getPredicate().equals(stmt.getPredicate())) {
                appendable.append(" ; ");
                appendPredicate(stmt.getPredicate());
                appendable.append(" ");                 

            } else {
                appendable.append(" , ");
            }                

            append(stmt.getObject());           
            last = stmt;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }    
    }

}
