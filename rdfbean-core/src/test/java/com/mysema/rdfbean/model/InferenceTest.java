package com.mysema.rdfbean.model;

import org.junit.Test;


public class InferenceTest {
    
    @Test
    public void test(){
	for (Inference inf : Inference.values()){
	    System.out.println(inf);
	}
    }

}