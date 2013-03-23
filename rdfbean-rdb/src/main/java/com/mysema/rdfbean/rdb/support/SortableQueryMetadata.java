package com.mysema.rdfbean.rdb.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinExpression;
import com.mysema.query.JoinType;

/**
 * @author tiwe
 * 
 */
public class SortableQueryMetadata extends DefaultQueryMetadata {

    private static final long serialVersionUID = 6326236143414219377L;

    private static final Pattern SPLIT = Pattern.compile("_");

    public SortableQueryMetadata() {
        this.noValidate();
    }

    @Override
    public List<JoinExpression> getJoins() {
        List<JoinExpression> joins = super.getJoins();
        List<JoinExpression> rv = new ArrayList<JoinExpression>(joins.size());
        for (JoinExpression join : joins) {
            if (join.getType() == JoinType.DEFAULT) {
                rv.add(join);
            } else {
                String[] path = SPLIT.split(join.getTarget().toString());
                boolean added = false;
                for (int i = rv.size() - 1; i >= 0 && !added; i--) {
                    String[] joinPath = SPLIT.split(rv.get(i).getTarget().toString());
                    if (path[0].equals(joinPath[0])) {
                        rv.add(i + 1, join);
                        added = true;
                    }
                }
                if (!added) {
                    rv.add(join);
                }
            }
        }
        return rv;
    }
}
