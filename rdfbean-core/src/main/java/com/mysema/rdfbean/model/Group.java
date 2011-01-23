package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class Group implements Block{
    
    private static final long serialVersionUID = 114999121944301068L;

    public static Group create(Block... blocks){
        Group group = new Group();
        group.blocks.addAll(Arrays.asList(blocks));
        return group;
    }
    
    public static Group optional(Block... blocks){
        Group group = create(blocks);
        group.optional = true;
        return group;
    }

    private final List<Block> blocks = new ArrayList<Block>();
    
    private final List<Predicate> filters = new ArrayList<Predicate>();
    
    private boolean optional;
    
    private Expression<UID> context;

    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }
    
}
