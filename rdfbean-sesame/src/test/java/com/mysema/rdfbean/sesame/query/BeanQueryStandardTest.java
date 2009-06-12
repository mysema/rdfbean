package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.StandardTest;
import com.mysema.query.StandardTestData;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.ENumber;
import com.mysema.query.types.expr.EString;
import com.mysema.query.types.expr.Expr;

/**
 * BeanQueryStandardTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryStandardTest extends AbstractSesameQueryTest implements StandardTest{
    
    private String knownValue = "propertymap";
    
    protected QSimpleType v1 = new QSimpleType("v1");
    
    protected QSimpleType v2 = new QSimpleType("v2");

    @Test
    public void booleanFilters(){
        for (EBoolean f : StandardTestData.booleanFilters(v1.directProperty.isNull(), v2.numericProperty.isNotNull())){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }
    
    @Test
    public void collectionFilters() {
        SimpleType2 instance = newQuery().from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);
        for (EBoolean f : StandardTestData.collectionFilters(v1.listProperty, v2.listProperty, instance)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }          
    }
    
    @Test
    @Ignore
    public void collectionProjections() {
        // FIXME : list size is not supported
        SimpleType2 instance = newQuery().from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);
        for (Expr<?> pr : StandardTestData.collectionProjections(v1.listProperty, v2.listProperty, instance)){
            System.err.println("\n" + pr);
            newQuery().from(v1).from(v2).list(pr);
        }         
    }
    
    @Test
    public void dateProjections() {
        // TODO        
    }
    
    @Test
    public void dateTimeFilters() {
        for (EBoolean f : StandardTestData.dateTimeFilters(v1.dateProperty, v2.dateProperty, new Date())){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
        
    }

    @Test
    public void dateTimeProjections() {
        for (Expr<?> pr : StandardTestData.dateTimeProjections(v1.dateProperty, v2.dateProperty, new Date())){
            System.err.println("\n" + pr);
            newQuery().from(v1).from(v2).list(pr);
        }        
    }

    @Test
    public void listFilters() {
        SimpleType2 instance = newQuery().from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);
        for (EBoolean f : StandardTestData.listFilters(v1.listProperty, v2.listProperty, instance)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }        
    }

    @Test
    @Ignore
    public void listProjections() {
        // FIXME : list size is not supported
        SimpleType2 instance = newQuery().from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);
        for (Expr<?> pr : StandardTestData.listProjections(v1.listProperty, v2.listProperty, instance)){
            System.err.println("\n" + pr);
            newQuery().from(v1).from(v2).list(pr);
        }                 
    }

    @Test
    public void mapFilters() {
        // TODO           
    }

    @Test
    public void mapProjections() {
        // TODO   
    }

    @Test
    public void numericCasts() {
        for (ENumber<?> num : StandardTestData.numericCasts(v1.numericProperty, v2.numericProperty, 1)){
            System.err.println("\n" + num);
            newQuery().from(v1).from(v2).list(num);
        }        
    }

    @Test
    public void numericFilters() {
        for (EBoolean f : StandardTestData.numericFilters(v1.numericProperty, v2.numericProperty, 10)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }        
    }

    @Test
    public void numericMatchingFilters() {
        for (EBoolean f : StandardTestData.numericMatchingFilters(v1.numericProperty, v2.numericProperty, 10)){
            System.err.println("\n" + f);
            assertTrue(f +" failed", !newQuery().from(v1).from(v2).where(f).list(v1.directProperty).isEmpty());
        }  
    }

    @Test
    public void numericProjections(){
        for (ENumber<?> num : StandardTestData.numericProjections(v1.numericProperty, v2.numericProperty, 10)){
            System.err.println("\n" + num);
            newQuery().from(v1).from(v2).list(num);
        }
    }

    @Test    
    public void stringFilters(){
        for (EBoolean f : StandardTestData.stringFilters(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            newQuery().from(v1).from(v2).where(f).list(v1.directProperty);
        }
    }

    @Test
    public void stringMatchingFilters(){
        for (EBoolean f : StandardTestData.stringMatchingFilters(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + f);
            assertTrue(f +" failed", !newQuery().from(v1).from(v2).where(f).list(v1.directProperty).isEmpty());
        }
    }

    @Test
    public void stringProjections(){               
        for (EString str : StandardTestData.stringProjections(v1.directProperty, v2.directProperty, knownValue)){
            System.err.println("\n" + str);
            newQuery().from(v1).from(v2).list(str);
        }
    }

    @Test
    public void timeProjections() {
        // TODO
        
    }
}
