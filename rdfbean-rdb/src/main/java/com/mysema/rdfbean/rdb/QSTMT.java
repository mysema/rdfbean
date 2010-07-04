package com.mysema.rdfbean.rdb;

import com.mysema.query.types.EConstructor;
import com.mysema.query.types.Expr;
import com.mysema.rdfbean.model.STMT;

/**
 * QSTMT provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class QSTMT extends EConstructor<STMT>{

    private static final long serialVersionUID = -7788473967920361945L;

    public QSTMT(Expr<?>... args){
        super(STMT.class, new Class[0], args);
    }
    
    public abstract STMT newInstance(Object... args);

}
