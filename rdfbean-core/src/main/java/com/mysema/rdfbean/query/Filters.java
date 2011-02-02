package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.GroupBlock;

public class Filters {
    
    private Set<Predicate> filters = new LinkedHashSet<Predicate>();
    
    private Set<Block> optBlocks = new LinkedHashSet<Block>();
    
    private Set<Predicate> optFilters = new LinkedHashSet<Predicate>();
    
    private boolean inOptional = false;
    
    public Set<Predicate> getFilters(){
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
            filters.add(Blocks.optional(
                    new ArrayList<Block>(optBlocks), 
                    optFilters.toArray(new Predicate[optFilters.size()])));
        }
        optBlocks = new LinkedHashSet<Block>();
        optFilters = new LinkedHashSet<Predicate>();
        inOptional = false;
    }
    
    public Predicate[] toArray(){
        return filters.toArray(new Predicate[filters.size()]);
    }

    public Block asBlock() {
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
