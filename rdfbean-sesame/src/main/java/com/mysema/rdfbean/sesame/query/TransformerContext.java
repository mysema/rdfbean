/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Locale;

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
 * TransformerContext provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface TransformerContext {
    
    Var createVar();
            
    Locale getCurrentLocale();

    MappedPath getMappedPath(Path<?> parent);

    Scope getPatternScope();

    ID getResourceForLID(Expr<?> arg);

    ValueFactory getValueFactory();

    boolean inNegation();

    boolean inOptionalPath();

    boolean isRegistered(Path<?> path);

    void match(JoinBuilder builder, Var s, UID p, Var o);

    void match(Var s, UID p, Var o);

    void register(Path<?> otherPath, Var var);

    TupleExpr toTuples(SubQuery arg0);

    ValueExpr toValue(Expr<?> expr);

    Value toValue(ID id);

    Var toVar(Constant<?> arg1);

    Var toVar(Path<?> path);

    Var toVar(UID id);

    Var toVar(Value value);
    
}
