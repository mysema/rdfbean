package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.List;

import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;

/**
 * @author tiwe
 *
 */
public class QLIT extends QNODE<LIT>{

    private static final List<UID> NUMERIC = Arrays.asList(
            XSD.decimalType, XSD.doubleType, XSD.floatType,
            XSD.integerType, XSD.longType, XSD.intType, XSD.shortType, XSD.byteType);

    private static final long serialVersionUID = 5748245169418342031L;

    public QLIT(String variable) {
        super(LIT.class, variable);
    }

    private static Constant<LIT> literal(String val){
        return new ConstantImpl<LIT>(LIT.class, new LIT(val));
    }

    private static Constant<LIT> literal(LIT val) {
        return new ConstantImpl<LIT>(LIT.class, val);
    }

    public BooleanExpression lt(String val){
        return BooleanOperation.create(Ops.BEFORE, this, literal(val));
    }

    public BooleanExpression gt(String val){
        return BooleanOperation.create(Ops.AFTER, this, literal(val));
    }

    public BooleanExpression loe(String val){
        return BooleanOperation.create(Ops.BOE, this, literal(val));
    }

    public BooleanExpression goe(String val){
        return BooleanOperation.create(Ops.AOE, this, literal(val));
    }

    public BooleanExpression lt(LIT val){
        if (NUMERIC.contains(val.getDatatype())){
            return BooleanOperation.create(Ops.LT,  this, literal(val));
        }else{
            return BooleanOperation.create(Ops.BEFORE,  this, literal(val));
        }
    }

    public BooleanExpression gt(LIT val){
        if (NUMERIC.contains(val.getDatatype())){
            return BooleanOperation.create(Ops.GT,  this, literal(val));
        }else{
            return BooleanOperation.create(Ops.AFTER,  this, literal(val));
        }
    }

    public BooleanExpression loe(LIT val){
        if (NUMERIC.contains(val.getDatatype())){
            return BooleanOperation.create(Ops.LOE, this, literal(val));
        }else{
            return BooleanOperation.create(Ops.BOE, this, literal(val));
        }
    }

    public BooleanExpression goe(LIT val){
        if (NUMERIC.contains(val.getDatatype())){
            return BooleanOperation.create(Ops.GOE, this, literal(val));
        }else{
            return BooleanOperation.create(Ops.AOE, this, literal(val));
        }
    }

    public BooleanExpression like(String val) {
       return BooleanOperation.create(Ops.LIKE, this, literal(val));
    }

    public BooleanExpression matches(String val) {
        return BooleanOperation.create(Ops.MATCHES, this, literal(val));
     }

    public Predicate isEmpty() {
        return BooleanOperation.create(Ops.STRING_IS_EMPTY, this);
    }

    public Predicate eqIgnoreCase(String val) {
        return BooleanOperation.create(Ops.EQ_IGNORE_CASE, this, literal(val));
    }

}
