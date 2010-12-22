package com.mysema.rdfbean.virtuoso;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public class STMTIterator implements CloseableIterator<STMT>{

    private final Converter converter;

    private final Statement stmt;
    
    private final ResultSet rs;

    @Nullable
    private final ID subject;

    @Nullable
    private final UID predicate;

    @Nullable
    private final NODE object;

    private final UID defaultGraph;

    @Nullable
    private Boolean next;

    private int col_g = -1;

    private int col_s = -1;

    private int col_p = -1;

    private int col_o = -1;

    public STMTIterator(Converter converter, Statement stmt, ResultSet rs, @Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, UID defaultGraph){
        this.converter = converter;
        this.stmt = stmt;
        this.rs = rs;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.defaultGraph = defaultGraph;
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String label = rsmd.getColumnName(i);
                if (label.equalsIgnoreCase("g")){
                    col_g = i;
                }else if (label.equalsIgnoreCase("s")){
                    col_s = i;
                }else if (label.equalsIgnoreCase("p")){
                    col_p = i;
                }else if (label.equalsIgnoreCase("o")){
                    col_o = i;
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public void close() {
        AbstractQueryImpl.close(stmt, rs);
    }

    @Override
    public boolean hasNext() {
        if (next == null){
            try {
                next = rs.next();
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }
        return next;
    }

    @Override
    public STMT next() {
        if (hasNext()){
            next = null;
            try {
                return extractRow();
            } catch (SQLException e) {
                throw new RepositoryException(e);
            }
        }else{
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected STMT extractRow() throws SQLException {
        UID _graph = null;
        ID _subject = subject;
        UID _predicate = predicate;
        NODE _object = object;
        Object val = null;

        try {
            if (col_g != -1) {
                val = rs.getObject(col_g);
                _graph = (UID) converter.toNODE(val);
                if (defaultGraph.equals(_graph)){
                    _graph = null;
                }
            }
        } catch (ClassCastException ccex) {
            throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting UID: "+ val, ccex);
        }

        if (_subject == null){
            try {
                val = rs.getObject(col_s);
                _subject = (ID) converter.toNODE(val);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting ID: " + val, ccex);
            }
        }            

        if (_predicate == null){
            try {
                val = rs.getObject(col_p);
                _predicate = (UID) converter.toNODE(val);
            } catch (ClassCastException ccex) {
                throw new IllegalArgumentException("Unexpected resource type encountered. Was expecting UID: "+ val, ccex);
            }   
        }         

        if (_object == null){
            _object = converter.toNODE(rs.getObject(col_o));
        }            

        return new STMT(_subject, _predicate, _object, _graph);
    }


}
