package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.GroupBlock;

public class Filters {
    
    private List<Predicate> filters = new ArrayList<Predicate>();
    
    private List<Block> optBlocks = new ArrayList<Block>();
    
    private List<Predicate> optFilters = new ArrayList<Predicate>();
    
    private boolean inOptional = false;
    
    public List<Predicate> getFilters(){
        return filters;
    }

    public int size() {
        return filters.size();
    }

    public void add(Predicate predicate) {
       if (inOptional){
           if (predicate instanceof Block){
               optBlocks.add((Block)predicate);
           }else{
               optFilters.add(predicate);
           }
       }else{
           filters.add(predicate);    
       }
        
    }
    
    public boolean inOptional(){
        return inOptional;
    }

    public void beginOptional() {
        inOptional = true;
    }
    
    public void endOptional() {
        if (optBlocks.isEmpty()){
            filters.addAll(optFilters);
        }else{
            filters.add(Blocks.optional(optBlocks, optFilters.toArray(new Predicate[optFilters.size()])));
        }
        optBlocks = new ArrayList<Block>();
        optFilters = new ArrayList<Predicate>();
        inOptional = false;
    }
    
    public Predicate[] toArray(){
        return filters.toArray(new Predicate[filters.size()]);
    }

    public Expression<?> asBlock() {
        List<Block> b = new ArrayList<Block>();
        List<Predicate> f = new ArrayList<Predicate>();
        for (Predicate filter : filters){
            if (filter instanceof Block){
                b.add((Block)filter);
            }else{
                f.add(filter);
            }
        }
        return new GroupBlock(b, f.toArray(new Predicate[f.size()]));
    }
    
}
