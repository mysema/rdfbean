/**
 *
 */
package com.mysema.rdfbean.rdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionBase;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.Visitor;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 *
 */
public class TupleFactoryExpression extends ExpressionBase<Map<String, NODE>> implements FactoryExpression<Map<String, NODE>>{

    private static final long serialVersionUID = -3344381241177858414L;

    private final ConverterRegistry converters;

    private final List<String> variables;

    private final List<Expression<?>> projection;

    private final Transformer<Long, NODE> transformer;

    @SuppressWarnings("unchecked")
    public TupleFactoryExpression(
            ConverterRegistry converters,
            List<String> variables,
            List<Expression<?>> pr,
            Transformer<Long, NODE> transformer) {
        super((Class)Map.class);
        this.converters = converters;
        this.variables = variables;
        this.projection = pr;
        this.transformer = transformer;
    }

    @Override
    public List<Expression<?>> getArgs() {
        return projection;
    }

    @Override
    public Map<String, NODE> newInstance(Object... args) {
        Map<String, NODE> rv = new HashMap<String, NODE>(args.length);
        for (int i = 0; i < args.length; i++){
            if (args[i] != null){
                if (projection.get(i) instanceof Operation<?>
                 || projection.get(i) instanceof TemplateExpression<?>){
                    String val = converters.toString(args[i]);
                    UID dtype = converters.getDatatype(args[i].getClass());
                    rv.put(variables.get(i), new LIT(val, dtype));
                }else if (args[i] instanceof String){    
                    String val = (String)args[i];
                    rv.put(variables.get(i), val.contains(":") ? new UID(val) : new BID(val));               
                }else{
                    rv.put(variables.get(i), transformer.transform((Long)args[i]));
                }
            }
        }
        return rv;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

}