package com.mysema.rdfbean.rdb.support;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinExpression;
import com.mysema.query.JoinType;
import com.mysema.query.types.Expr;
import com.mysema.query.types.expr.EBoolean;

/**
 * @author tiwe
 *
 */
public class SortableQueryMetadata extends DefaultQueryMetadata{
    
    private static final long serialVersionUID = 6326236143414219377L;

    private List<JoinExpression> joins = new ArrayList<JoinExpression>();
    
    @Nullable
    private JoinExpression last;
    
    @Override
    public void addJoin(JoinExpression join){
        if (join.getType() == JoinType.DEFAULT){
            joins.add(join);
        }else{
            String[] path = join.getTarget().toString().split("_");
            boolean added = false;
            for (int i = joins.size()-1; i >= 0 && !added; i--){
                String[] joinPath = joins.get(i).getTarget().toString().split("_");
                if (path[0].equals(joinPath[0])){
                    joins.add(i+1, join);
                    added = true;
                }                
            }
            if (!added){
                joins.add(join);
            }
        }
        last = join;
    }
    
    @Override
    public void addJoin(JoinType joinType, Expr<?> expr) {
        addJoin(new JoinExpression(joinType, expr));
    }

    @Override
    public void addJoinCondition(EBoolean o) {
        if (last != null){
            last.addCondition(o);
        }
    }
    
    @Override
    public List<JoinExpression> getJoins() {
        return joins;
    }
}
