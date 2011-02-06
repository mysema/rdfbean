package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.types.Predicate;

public interface ContainerBlock extends Block{
    
    List<Block> getBlocks();

    @Nullable
    Predicate getFilters();

}
