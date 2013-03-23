package com.mysema.rdfbean.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.Path;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.TemplateExpression;

public class NamespaceCollector implements RDFVisitor<Void, Void> {

    private final Set<String> namespaces = new HashSet<String>();

    @Override
    public Void visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        return visit(md);
    }

    @Nullable
    private Void visit(QueryMetadata md) {
        // select
        handle(md.getProjection());
        // from
        for (JoinExpression join : md.getJoins()) {
            join.getTarget().accept(this, null);
        }
        // where
        handle(md.getWhere());
        // group
        handle(md.getGroupBy());
        // having
        handle(md.getHaving());
        return null;
    }

    @Override
    public Void visit(UnionBlock expr, Void context) {
        handle(expr.getBlocks());
        return null;
    }

    @Override
    public Void visit(GroupBlock expr, Void context) {
        handle(expr.getBlocks());
        handle(expr.getFilters());
        return null;
    }

    @Override
    public Void visit(GraphBlock expr, Void context) {
        handle(expr.getBlocks());
        handle(expr.getContext());
        return null;
    }

    @Override
    public Void visit(OptionalBlock expr, Void context) {
        handle(expr.getBlocks());
        handle(expr.getFilters());
        return null;
    }

    @Override
    public Void visit(PatternBlock expr, Void context) {
        handle(expr.getSubject());
        handle(expr.getPredicate());
        handle(expr.getObject());
        handle(expr.getContext());
        return null;
    }

    @Override
    public Void visit(Constant<?> expr, Void context) {
        Object o = expr.getConstant();
        if (o instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) o;
            for (Object c : col) {
                if (c instanceof UID) {
                    namespaces.add(((UID) c).ns());
                }
            }
        } else if (o instanceof UID) {
            namespaces.add(((UID) o).ns());
        }
        return null;
    }

    @Override
    public Void visit(FactoryExpression<?> expr, Void context) {
        return null;
    }

    @Override
    public Void visit(Operation<?> expr, Void context) {
        handle(expr.getArgs());
        return null;
    }

    @Override
    public Void visit(ParamExpression<?> expr, Void context) {
        return null;
    }

    @Override
    public Void visit(Path<?> expr, Void context) {
        if (expr.getMetadata().getElement() instanceof Expression) {
            handle((Expression) expr.getMetadata().getElement());
        }
        handle(expr.getMetadata().getParent());
        return null;
    }

    @Override
    public Void visit(SubQueryExpression<?> expr, Void context) {
        return visit(expr.getMetadata());
    }

    @Override
    public Void visit(TemplateExpression<?> expr, Void context) {
        handle(expr.getArgs());
        return null;
    }

    private void handle(@Nullable Expression<?> e) {
        if (e != null) {
            e.accept(this, null);
        }
    }

    private void handle(List<?> args) {
        for (Object arg : args) {
            if (arg instanceof Expression) {
                ((Expression) arg).accept(this, null);
            }
        }
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }

}
