package com.mysema.rdfbean.model;

import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Ops;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.path.SimplePath;

/**
 * @author tiwe
 *
 * @param <T>
 */
public class QNODE<T extends NODE> extends SimplePath<T>{

    private static final long serialVersionUID = 1134119241723346776L;

    public QNODE(Class<T> type, String variable) {
        super(type, variable);
    }
    
    public QNODE(Class<T> type, PathMetadata<?> metadata) {
        super(type, metadata);
    }
    
    private static Constant<LIT> literal(String val){
        return new ConstantImpl<LIT>(LIT.class, new LIT(val));
    }
    
    private static Constant<LIT> literal(LIT val) {
        return new ConstantImpl<LIT>(LIT.class, val);
    }
    
    public BooleanExpression lt(String val){
        return BooleanOperation.create(Ops.LT, this, literal(val));
    }
    
    public BooleanExpression gt(String val){
        return BooleanOperation.create(Ops.GT, this, literal(val));
    }
    
    public BooleanExpression loe(String val){
        return BooleanOperation.create(Ops.LOE, this, literal(val));
    }
    
    public BooleanExpression goe(String val){
        return BooleanOperation.create(Ops.GOE, this, literal(val));
    }
    
    public BooleanExpression lt(LIT val){
        return BooleanOperation.create(Ops.LT,  this, literal(val));
    }
    
    public BooleanExpression gt(LIT val){
        return BooleanOperation.create(Ops.GT,  this, literal(val));
    }
    
    public BooleanExpression loe(LIT val){
        return BooleanOperation.create(Ops.LOE, this, literal(val));
    }
    
    public BooleanExpression goe(LIT val){
        return BooleanOperation.create(Ops.GOE, this, literal(val));
    }
        
}
