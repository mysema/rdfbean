package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public class RDFUpdateImpl implements RDFUpdate {

    private final RDFConnection connection;
    
    private final List<PatternBlock> delete = new ArrayList<PatternBlock>();
    
    private final List<PatternBlock> insert = new ArrayList<PatternBlock>();
    
    private final List<UID> from = new ArrayList<UID>();
    
    private final List<UID> into = new ArrayList<UID>();
    
    private final List<Block> where = new ArrayList<Block>();
    
    public RDFUpdateImpl(RDFConnection connection) {
        this.connection = connection;
    }
  
    @Override
    public void execute() {
        UID[] _from = from.toArray(new UID[from.size()]);
        Block[] _where = where.toArray(new Block[where.size()]);
        List<STMT> added = new ArrayList<STMT>();
        List<STMT> removed = new ArrayList<STMT>();
        
        if (!insert.isEmpty()){            
            CloseableIterator<STMT> stmts = new RDFQueryImpl(connection)
                .from(_from).where(_where)
                .construct(insert.toArray(new Block[insert.size()]));            
            convertStatements(stmts, added);            
        }
        
        if (!delete.isEmpty()){
            CloseableIterator<STMT> stmts = new RDFQueryImpl(connection)
                .from(_from).where(_where)
                .construct(delete.toArray(new Block[insert.size()]));            
            convertStatements(stmts, removed);
        }
        
        connection.update(removed, added);
    }

    private void convertStatements(CloseableIterator<STMT> stmts, List<STMT> target) {
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                for (UID uid : into){
                    target.add(new STMT(stmt, uid));
                }
            }
        }finally{
            stmts.close();
        }
    }
    
    @Override
    public RDFUpdate delete(PatternBlock... patterns) {
        this.delete.addAll(Arrays.asList(patterns));
        return this;
    }

    @Override
    public RDFUpdate from(UID... uids) {
        this.from.addAll(Arrays.asList(uids));
        return this;
    }

    @Override
    public RDFUpdate insert(PatternBlock... patterns) {
        this.insert.addAll(Arrays.asList(patterns));
        return this;
    }

    @Override
    public RDFUpdate into(UID... uids) {
        this.into.addAll(Arrays.asList(uids));
        return this;
    }

    @Override
    public RDFUpdate where(Block... blocks) {
        this.where.addAll(Arrays.asList(blocks));
        return this;
    }

}
