/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Locale;

import javax.annotation.Nullable;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.StatementPattern.Scope;

import com.mysema.query.types.expr.Constant;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.path.Path;
import com.mysema.query.types.query.SubQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.MappedPath;

/**
 * TransformerContext provides a limited view of SesameQuery methods to OperationTransformer instances
 *
 * @author tiwe
 * @version $Id$
 */
public interface TransformerContext {
    
    /**
     * Create a new Variable
     */
    Var createVar();
            
    /**
     * Get the Locale
     */
    Locale getLocale();

    /**
     * Get the mapped path for the given path
     */
    MappedPath getMappedPath(Path<?> parent);

    /**
     * Get the pattern scope
     */
    Scope getPatternScope();

    /**
     * Get the resource for the given expression
     */
    ID getResourceForLID(Constant<String> arg);

    /**
     * Get the ValueFactory
     */
    ValueFactory getValueFactory();

    /**
     * @return
     */
    boolean inNegation();

    /**
     * @return
     */
    boolean inOptionalPath();

    /**
     * @param path
     * @return
     */
    boolean isKnown(Path<?> path);

    /**
     * @param builder
     * @param s
     * @param p
     * @param o
     */
    void match(JoinBuilder builder, Var s, UID p, Var o);

    /**
     * @param s
     * @param p
     * @param o
     */
    void match(Var s, UID p, Var o);

    /**
     * @param otherPath
     * @param var
     */
    void register(Path<?> otherPath, Var var);

    /**
     * Transform the given SubQuery to a TupleExpr
     * 
     * @param arg0
     * @return
     */
    TupleExpr toTuples(SubQuery arg0);

    /**
     * Transform the given Expr to a ValueExpr
     * 
     * @param expr
     * @return
     */
    @Nullable
    ValueExpr toValue(Expr<?> expr);

    /**
     * Transform the given ID to a Value
     * 
     * @param id
     * @return
     */
    Value toValue(ID id);

    /**
     * Transform the given Constant to a Var
     * 
     * @param arg1
     * @return
     */
    Var toVar(Constant<?> arg1);

    /**
     * Transform the given Path to a Var
     * 
     * @param path
     * @return
     */
    Var toVar(Path<?> path);

    /**
     * Transform the given UID to a Var
     * 
     * @param id
     * @return
     */
    Var toVar(UID id);

    /**
     * Transform the given Value to a Var
     * 
     * @param value
     * @return
     */
    Var toVar(Value value);

    /**
     * Create a new JoinBuilder
     * 
     * @return
     */
    JoinBuilder createJoinBuilder();
    
}
