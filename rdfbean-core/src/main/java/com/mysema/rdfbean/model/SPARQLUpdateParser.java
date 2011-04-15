package com.mysema.rdfbean.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.CharSet;


/**
 * @author tiwe
 *
 */
public class SPARQLUpdateParser {
    
    private static final CharSet GT = CharSet.getInstance(">");
    
    private static final CharSet LT = CharSet.getInstance("<");
    
    private static final CharSet COLON = CharSet.getInstance(":");
    
    private static final CharSet BLOCK_START = CharSet.getInstance("{");
    
    private static final CharSet BLOCK_END = CharSet.getInstance("}");
    
    private static final CharSet WS = CharSet.getInstance(" \t\n\f\r");
    
    private static final CharSet WORD = CharSet.getInstance("a-zA-Z0-9");
    
    private static final CharSet ALPHA = CharSet.getInstance("A-Z");
    
    private static final CharSet DATA = CharSet.getInstance("DATA");
    
    private static final CharSet DELETE = CharSet.getInstance("DELETE");
    
    private static final CharSet FROM = CharSet.getInstance("FROM");
    
    private static final CharSet GRAPH = CharSet.getInstance("GRAPH");
    
    private static final CharSet INTO = CharSet.getInstance("INTO");
    
    private static final CharSet INSERT = CharSet.getInstance("INSERT");
        
    private static final CharSet SILENT = CharSet.getInstance("SILENT");
    
    private static final CharSet WHERE = CharSet.getInstance("WHERE");   
    
    private static final CharSet P = CharSet.getInstance("P");
    
    private static final CharSet PREFIX = CharSet.getInstance("PREFIX");
    
    private Map<String,String> prefixes;
    
    private int ch;

    private PushbackReader in;

    private int recentIndex;

    private char[] recentRead;

    private int row;

    private StringBuilder sb;
    
    public UpdateClause parse(String str) throws IOException {
        return parse(new ByteArrayInputStream(str.getBytes("UTF-8")), "UTF-8");
    }
    
    public UpdateClause parse(InputStream in) throws IOException {
        return parse(in, "UTF-8");
    }

    public UpdateClause parse(InputStream in, String charset) throws IOException {
        try {
            this.in = new PushbackReader(new InputStreamReader(in, charset), 1);
            init();
            while (nextChar().in(P)){
                skipWhileIn(PREFIX, WS);
                String prefix = collectWhileIn(WORD);
                skipWhileIn(COLON, WS);
                UID ns = uri();
                prefixes.put(prefix, ns.getId());
                skipWhileIn(WS);
            }            
            pushback();
            String start = collectWhileIn(ALPHA);
            skipWhitespace();
            if (start.equals("CLEAR")){                
                return clear();
            }else if (start.equals("CREATE")){
                return create();
            }else if (start.equals("DELETE")){
                return delete();
            }else if (start.equals("DROP")){
                return drop();
            }else if (start.equals("INSERT")){
                return insert();                 
            }else if (start.equals("LOAD")){
                return load();
            }else if (start.equals("MODIFY")){
                return modify();
            }else{
                throw new IllegalStateException("Illegal query start '" + start + "'"); 
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse query " + location(), e);
        } finally {
            in.close();
        }
    }
    
    private UpdateClause modify() throws IOException {
        // MODIFY [ <uri> ]* DELETE { template } INSERT { template } [ WHERE { pattern } ]
        UpdateClause modify = new UpdateClause(prefixes, UpdateClause.Type.MODIFY);
        nextChar();
        while (in(LT)){
            pushback();
            modify.addInto(uri());
            skipWhitespace();
            nextChar();
        }
        skipWhileIn(DELETE, WS);
        modify.setDelete(block());
        skipWhileIn(INSERT, WS);
        modify.setInsert(block());
        skipWhitespace();
        nextChar();
        if (ch > -1 && ch < 65535){ // FIXME
            skipWhileIn(WHERE, WS);
            modify.setPattern(block());    
        }        
        return modify;
    }

    private UpdateClause load() throws IOException {
        // LOAD <remoteURI> [ INTO <uri> ]
        UID remoteURI = uri();
        nextChar();
        if (ch > -1){
            skipWhileIn(WS, INTO);
            UID into = uri();
            return new UpdateClause(prefixes, UpdateClause.Type.LOAD, remoteURI, into);
        }else{
            return new UpdateClause(prefixes, UpdateClause.Type.LOAD, remoteURI, null);    
        }        
    }
    
    private UpdateClause insert() throws IOException {
        // INSERT DATA [ INTO <uri> ]* { triples }
        // INSERT [ INTO <uri> ]* { template } [ WHERE { pattern } ]
        String token = collectWhileIn(DATA, INTO);
        UpdateClause insert = new UpdateClause(prefixes, UpdateClause.Type.INSERT);
        if (token.equals("DATA")){
            insert.addInto(into());
            insert.setTemplate(block());
            
        }else {
            if (token.equals("INTO")) {
                skipWhitespace();
                insert.addInto(uri());
                insert.addInto(into());
                skipWhitespace();
            }
            insert.setTemplate(block());
            skipWhileIn(WS);
            if (nextChar().in(WHERE)){
                skipWhileIn(WS, WHERE);            
                insert.setPattern(block());    
            }                        
        }
        return insert;
    }

    private UpdateClause drop() throws IOException {
        // DROP [ SILENT ] GRAPH <uri>
        String token = collectWhileIn(SILENT);
        skipWhitespace();
        skipWhileIn(GRAPH);
        skipWhitespace();
        UID uid = uri();
        return new UpdateClause(prefixes, UpdateClause.Type.DROP, uid, !token.isEmpty());
    }

    private UpdateClause delete() throws IOException {
        // DELETE DATA [ FROM <uri> ]* { triples }
        // DELETE [ FROM <uri> ]* { template } [ WHERE { pattern } ]
        String token = collectWhileIn(DATA, FROM);
        UpdateClause delete = new UpdateClause(prefixes, UpdateClause.Type.DELETE);
        if (token.equals("DATA")){
            delete.addFrom(from());
            delete.setTemplate(block());
            
        }else {
            if (token.equals("FROM")) {
                skipWhitespace();
                delete.addFrom(uri());
                delete.addFrom(from());
                skipWhitespace();
            }
            delete.setTemplate(block());            
            skipWhileIn(WS, WHERE);
            delete.setPattern(block());            
        }
        return delete;
    }
    
    private String block() throws IOException{
        expect(BLOCK_START);
        String rv = collectWhileNotIn(BLOCK_END);
        expect(BLOCK_END);
        return rv;
    }
    
    private List<UID> from() throws IOException{
        return collectUIDs(FROM);
    }
    
    private List<UID> into() throws IOException{
        return collectUIDs(INTO);
    }
    
    private List<UID> collectUIDs(CharSet cs) throws IOException{
        List<UID> uids = new ArrayList<UID>();
        String token = null;
        do {
            skipWhitespace();
            token = collectWhileIn(cs);
            skipWhitespace();
            if (!token.isEmpty()){
                UID uid = uri();
                uids.add(uid);
            }
        }while (!token.isEmpty());
        return uids;
    }

    private UpdateClause create() throws IOException {
        // CREATE [ SILENT ] GRAPH <uri>
        String token = collectWhileIn(SILENT);
        skipWhitespace();
        skipWhileIn(GRAPH);
        skipWhitespace();
        UID uid = uri();
        return new UpdateClause(prefixes, UpdateClause.Type.CREATE, uid, !token.isEmpty());
    }

    private UpdateClause clear() throws IOException {
        // CLEAR [ GRAPH <uri> ]
        skipWhitespace();
        nextChar();
        if (ch > -1 && ch < 65535){ // FIXME
            skipWhileIn(GRAPH, WS);
            UID uid = uri();
            return new UpdateClause(prefixes, UpdateClause.Type.CLEAR, uid);
        }else{
            return new UpdateClause(prefixes, UpdateClause.Type.CLEAR);
        }
    }
    
    private UID uri() throws IOException{
        expect(LT);
        UID rv = new UID(collectWhileNotIn(WS, GT));
        expect(GT);
        return rv;
    }

    public String collectWhileIn(CharSet... charSets) throws IOException {
        sb.setLength(0);
        while (nextChar().in(charSets)) {
            sb.append((char) ch);
        }
        pushback();
        return sb.toString();
    }    

    public String collectWhileNotIn(CharSet... charSets) throws IOException {
        sb.setLength(0);
        while (!nextChar().in(charSets)) {
            sb.append((char) ch);
        }
        pushback();
        return sb.toString();
    }

    private void expect(CharSet charSet) throws IOException {
        if (nextChar().notIn(charSet)) {
            throw new IOException("Expected " + charSet + " found " + location());
        }
    }
    
    private boolean in(CharSet charSet) throws IOException {
        if (ch == -2) {
            throw new IOException("Advance first!");
        } else if (ch != -1) {
            return charSet.contains((char) ch);
        } else {
            return false;
        }
    }

    private boolean in(CharSet... charSets) throws IOException {
        for (CharSet cs : charSets) {
            if (in(cs)) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        prefixes = new HashMap<String, String>();
        ch = -2;
        sb = new StringBuilder(128);
        row = 1;
        recentRead = new char[20];
        recentIndex = -1;
    }

    private String location() {
        StringBuilder s = new StringBuilder(recentRead.length + 10);
        if (ch > 0) {
            s.append((char) ch);
        } else {
            s.append(ch);
        }
        s.append("@");
        s.append(row);
        s.append(": ");

        int startIndex = recentIndex+1 - recentRead.length;
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (; startIndex <= recentIndex; startIndex++) {
            s.append(recentRead[startIndex % recentRead.length]);
        }
        return s.toString();
    }

//    private boolean skipWhileNotIn(CharSet... charSets) throws IOException {
//        boolean found = false;
//        while (!nextChar().in(charSets)) {
//            found = true;
//        }
//        pushback();
//        return found;
//    }

    private SPARQLUpdateParser nextChar() throws IOException {
        ch = in.read();         
        if (ch == '\n') {
            row++;
        }
        if (ch != -1) {
            recentRead[++recentIndex % recentRead.length] = (char) ch;
        }
        return this;
    }

    private boolean notIn(CharSet charSet) throws IOException {
        if (ch == -2) {
            throw new IOException("Advance first!");
        } else if (ch != -1) {
            return !charSet.contains((char) ch);
        } else {
            return true;
        }
    }

    private void pushback() throws IOException {
        in.unread(ch);
        if (ch == '\n') {
            row--;
        }
        recentIndex--;

        ch = -2;
    }

    private boolean skipWhileIn(CharSet... charSets) throws IOException {
        boolean found = false;
        while (nextChar().in(charSets)) {
            found = true;
        }
        pushback();
        return found;
    }

    private void skipWhitespace() throws IOException {
        while (nextChar().in(WS)) ; //NOSONAR
        pushback();
    }

    @Override
    public String toString() {
        return location();
    }
}
