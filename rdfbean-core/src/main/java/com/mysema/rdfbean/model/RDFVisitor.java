package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public interface RDFVisitor<R,C> extends Visitor<R,C>{

    void visit(QueryMetadata md, QueryLanguage<?, ?> queryType);

    @Nullable
    R visit(UnionBlock expr, @Nullable C context);

    @Nullable
    R visit(GroupBlock expr, @Nullable C context);

    @Nullable
    R visit(GraphBlock expr, @Nullable C context);

    @Nullable
    R visit(OptionalBlock expr, @Nullable C context);

    @Nullable
    R visit(PatternBlock expr, @Nullable C context);

}