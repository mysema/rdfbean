package com.mysema.rdfbean.sesame.query;

import com.mysema.query.types.Constant;
import com.mysema.query.types.Custom;
import com.mysema.query.types.Expr;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Param;
import com.mysema.query.types.Path;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.Visitor;

/**
 * ExtractorVisitor provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ExtractorVisitor implements Visitor{

    private Expr<?> expr;
    
    @Override
    public void visit(Constant<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(Custom<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(FactoryExpression<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(Operation<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(Path<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(SubQueryExpression<?> e) {
        expr = e.asExpr();
    }

    @Override
    public void visit(Param<?> e) {
        expr = e.asExpr();        
    }
    
    public Expr<?> getExpression(){
        return expr;
    }

}
