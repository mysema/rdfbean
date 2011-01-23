package com.mysema.rdfbean.model;

import com.mysema.query.support.SerializerBase;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.Templates;

/**
 * @author tiwe
 *
 */
public class SPARQLVisitor extends SerializerBase<SPARQLVisitor>{
    
    public SPARQLVisitor(Templates templates) {
        super(templates);
    }

    @Override
    public Void visit(SubQueryExpression<?> expr, Void context) {
        // TODO Auto-generated method stub
        return null;
    }

    public Void visit(Group expr, Void context) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Void visit(Pattern expr, Void context) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
