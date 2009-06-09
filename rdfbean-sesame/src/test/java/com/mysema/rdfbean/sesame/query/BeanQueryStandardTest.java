package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.StandardTest;
import com.mysema.query.StandardTestData;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.ENumber;
import com.mysema.query.types.expr.EString;

/**
 * BeanQueryStandardTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryStandardTest extends AbstractSesameQueryTest implements StandardTest{
    
    private String knownValue = "propertymap";
    
    protected QTestType v1 = new QTestType("v1");
    
    protected QTestType v2 = new QTestType("v2");

    @Test
    @Ignore
    public void booleanFilters(){
        for (EBoolean f : StandardTestData.booleanFilters(v1.directProperty.isNull(), v2.numericProperty.isNotNull())){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }
    
    @Override
    public void collectionFilters() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void collectionProjections() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void dateProjections() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void dateTimeProjections() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void listFilters() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void listProjections() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mapFilters() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mapProjections() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void numericCasts() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void numericFilters() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void numericMatchingFilters() {
        // TODO Auto-generated method stub
        
    }

    @Test
    @Ignore
    public void numericProjections(){
        for (ENumber<?> num : StandardTestData.numericProjections(v1.numericProperty, v2.numericProperty, 1)){
            System.err.println("\n" + num);
            newQuery().from(v1).from(v2).list(num);
        }
    }

    @Test    
    @Ignore
    public void stringFilters(){
        for (EBoolean f : StandardTestData.stringFilters(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }

    @Test
    @Ignore
    public void stringMatchingFilters(){
        for (EBoolean f : StandardTestData.stringMatchingFilters(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            assertTrue(!newQuery().from(v1).from(v2).where(f).list(v1.directProperty).isEmpty());
        }
    }

    @Test
    @Ignore
    public void stringProjections(){               
        for (EString str : StandardTestData.stringProjections(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + str);
            newQuery().from(v1).from(v2).list(str);
        }
    }

    @Override
    public void timeProjections() {
        // TODO Auto-generated method stub
        
    }
}
