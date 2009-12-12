package com.mysema.rdfbean.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mysema.query.types.operation.Ops;


/**
 * FunctionsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class QueryFunctionsTest {
    
    @Test
    public void  test(){
        Set<String> functions = new HashSet<String>();
        for (Method m : QueryFunctions.class.getMethods()){
            functions.add(m.getName().toUpperCase());
        }
        
        List<Field> fields = new ArrayList<Field>();
        fields.addAll(Arrays.asList(Ops.class.getFields()));
        fields.addAll(Arrays.asList(Ops.AggOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.DateTimeOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.MathOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.StringOps.class.getFields()));
        
        int available = 0;
        int missing = 0;        
        for (Field field : fields){
            String name = field.getName().replace("_", "");
            if (!functions.contains(name)){
                missing++;
            }else{
                available++;
            }
        }
        System.err.println(missing + " functions missing, " + available + " available");
    }

}
