package com.mysema.rdfbean.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.SPARQLQuery;

/**
 * @author tiwe
 *
 */
public abstract class AbstractQueryImpl implements SPARQLQuery{

    private static final Pattern VARIABLE = Pattern.compile("\\?[a-zA-Z_]\\w*");

    private final Connection connection;

    private final int prefetch;

    protected PreparedStatement stmt;

    protected ResultSet rs;

    protected final String query;

    protected final Map<String, NODE> bindings = new HashMap<String,NODE>();

    public AbstractQueryImpl(Connection connection, int prefetch, String query) {
        this.connection = connection;
        this.prefetch = prefetch;
        this.query = query;
    }

    @Override
    public final void setBinding(String variable, NODE node) {
        bindings.put(variable, node);
    }

    @Override
    public final void setMaxQueryTime(int secs) {
        // do nothing
    }

    protected void close(){
        close(stmt, rs);
    }

    public static void close(@Nullable Statement stmt, @Nullable ResultSet rs){
        try{
            try {
                if (rs != null){
                    rs.close();
                }
            } finally {
                if (stmt != null){
                    stmt.close();
                }
            }
        }catch(SQLException e){
            throw new QueryException(e);
        }
    }

    protected ResultSet executeQuery(String query) throws SQLException{
        if (bindings.isEmpty()){
            stmt = connection.prepareStatement(query);
        }else{
            List<NODE> nodes = new ArrayList<NODE>(bindings.size());
            String normalized = normalize(query, bindings, nodes);
            stmt = connection.prepareStatement(normalized);
            int offset = 1;
            for (NODE node : nodes){
                if (node.isResource()){
                    VirtuosoRepositoryConnection.bindResource(stmt, offset++, node.asResource());
                }else{
                    VirtuosoRepositoryConnection.bindValue(stmt, offset, node.asLiteral());
                    offset += 3;
                }
            }
        }
        stmt.setFetchSize(prefetch);
        rs = stmt.executeQuery();
        return rs;
    }

    static String normalize(String query, Map<String, NODE> bindings, List<NODE> nodes) {
        String queryLower = query.toLowerCase(Locale.ENGLISH);
        Matcher matcher = VARIABLE.matcher(query);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()){
            String variable = matcher.group().substring(1);
            String replacement = matcher.group();
            boolean inFilter = inFilter(query, matcher);
            if (bindings.containsKey(variable) && queryLower.substring(0, matcher.start()).contains("where")){
                NODE node = bindings.get(variable);
                nodes.add(node);
                if (node.isResource()){
                    replacement = inFilter ? "iri(??)" : "`iri(??)`";
                }else{
                    replacement = inFilter ? "bif:__rdf_long_from_batch_params(??,??,??)" : "`bif:__rdf_long_from_batch_params(??,??,??)`";
                }
            }
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static boolean inFilter(String query, Matcher matcher) {
        int i;
        for (i = matcher.start()-1; i >= 0; i-- ){
            if (query.charAt(i) == ')') return false;
            if (query.charAt(i) == '(') break;
        }
        if (i > 0){
            for (i = matcher.end(); i < query.length(); i++ ){
                if (query.charAt(i) == '(') return false;
                if (query.charAt(i) == ')') return true;
            }
        }
        return false;
    }

}
