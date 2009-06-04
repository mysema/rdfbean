package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.TestExprs;
import com.mysema.query.TestFilters;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.ENumber;
import com.mysema.query.types.expr.EString;

/**
 * BeanQueryStandardTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryStandardTest extends AbstractSesameQueryTest{
    
    private String knownValue = "propertymap";
    
    protected QTestType v1 = new QTestType("v1");
    
    protected QTestType v2 = new QTestType("v2");

    @Test
    @Ignore
    public void stringProjections(){               
        for (EString str : TestExprs.getProjectionsForString(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + str);
            newQuery().from(v1).from(v2).list(str);
        }
    }
    
    @Test
    @Ignore
    public void testNumericProjections(){
        for (ENumber<?> num : TestExprs.getProjectionsForNumber(v1.numericProperty, v2.numericProperty, 1)){
            System.err.println("\n" + num);
            newQuery().from(v1).from(v2).list(num);
        }
    }
    
    @Test    
    @Ignore
    public void stringFilters(){
        for (EBoolean f : TestFilters.getFiltersForString(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }
    
    @Test
    @Ignore
    public void matchingStringFilters(){
        for (EBoolean f : TestFilters.getMatchingFilters(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            assertTrue(!newQuery().from(v1).from(v2).where(f).list(v1.directProperty).isEmpty());
        }
    }
    
    @Test
    @Ignore
    public void booleanFilters(){
        for (EBoolean f : TestFilters.getFiltersForBoolean(v1.directProperty.isNull(), v2.numericProperty.isNotNull())){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }
}
