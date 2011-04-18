package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        UpdateClause.Type type = null;
        if (delete.isEmpty()) {
            type = UpdateClause.Type.INSERT;
        } else if (insert.isEmpty()) {
            type = UpdateClause.Type.DELETE;
        } else {
            type = UpdateClause.Type.MODIFY;
        }
        UpdateClause updateClause = new UpdateClause(Collections.<String, String>emptyMap(), type);
        if (!delete.isEmpty()){
            updateClause.setDelete(delete.toString()); // TODO
        }
        if (!insert.isEmpty()){
            updateClause.setInsert(insert.toString()); // TODO   
        }
        updateClause.addFrom(from);
        updateClause.addInto(into);
        updateClause.setTemplate(where.toString()); // TODO
        
        // TODO : execute
    }
    
    @Override
    public RDFUpdate delete(PatternBlock... patterns) {
        this.delete.addAll(Arrays.asList(patterns));
        return this;
    }

    @Override
    public RDFUpdate from(UID uid) {
        this.from.add(uid);
        return this;
    }

    @Override
    public RDFUpdate insert(PatternBlock... patterns) {
        this.insert.addAll(Arrays.asList(patterns));
        return this;
    }

    @Override
    public RDFUpdate into(UID uid) {
        this.into.add(uid);
        return this;
    }

    @Override
    public RDFUpdate where(Block... blocks) {
        this.where.addAll(Arrays.asList(blocks));
        return this;
    }

}
